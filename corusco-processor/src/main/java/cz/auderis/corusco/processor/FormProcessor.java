package cz.auderis.corusco.processor;

import cz.auderis.corusco.annotations.form.CheckBox;
import cz.auderis.corusco.annotations.form.ComboBox;
import cz.auderis.corusco.annotations.form.CoruscoForm.ComponentState;
import cz.auderis.corusco.annotations.form.DateField;
import cz.auderis.corusco.annotations.form.CoruscoForm.DependsOn;
import cz.auderis.corusco.annotations.form.CoruscoForm.Option;
import cz.auderis.corusco.annotations.form.RadioGroup;
import cz.auderis.corusco.annotations.form.CoruscoForm;
import cz.auderis.corusco.annotations.form.TextField;
import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.validation.DecimalRange;
import cz.auderis.corusco.annotations.validation.IntRange;
import cz.auderis.corusco.annotations.validation.Length;
import cz.auderis.corusco.annotations.validation.Regex;
import cz.auderis.corusco.annotations.validation.Required;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Builds validated form specifications from {@link CoruscoForm} sources.
 *
 * <p>This processor owns source-shape validation and annotation normalization
 * for form records and abstract form contracts. It deliberately stops at
 * package-private specification objects; generated Java source remains the
 * responsibility of {@link GeneratedSourceWriter}.</p>
 */
final class FormProcessor {

    private final Elements elements;
    private final Types types;
    private final Filer filer;
    private final Messager messager;

    FormProcessor(Elements elements, Types types, Filer filer, Messager messager) {
        this.elements = elements;
        this.types = types;
        this.filer = filer;
        this.messager = messager;
    }

    void process(TypeElement formType) {
        FormSpec form = createSpec(formType);
        if (form != null) {
            sourceWriter().writeFormSources(formType, form);
        }
    }

    FormSpec createSpec(TypeElement formType) {
        CoruscoForm annotation = formType.getAnnotation(CoruscoForm.class);
        return createSpec(formType, annotation.id(), "@CoruscoForm", true);
    }

    FormSpec createSpec(
            TypeElement formType,
            String formId,
            String annotationName,
            boolean allowAbstractClasses
    ) {
        boolean supportedKind = formType.getKind() == ElementKind.RECORD
                || (allowAbstractClasses && formType.getKind() == ElementKind.CLASS);
        if (!supportedKind) {
            String supportedShapes = allowAbstractClasses ? "records or abstract classes" : "records";
            error(formType, annotationName + " is supported only on " + supportedShapes);
            return null;
        }
        if (formId.isBlank()) {
            error(formType, annotationName + " id must not be blank");
            return null;
        }
        if (!isStableId(formId)) {
            error(formType, annotationName + " id must contain only letters, digits, dots, underscores, dashes, or slashes");
            return null;
        }
        if (!formType.getTypeParameters().isEmpty()) {
            error(formType, annotationName + " generic source types are not supported by this processor stage");
            return null;
        }

        if (formType.getKind() == ElementKind.CLASS && !formType.getModifiers().contains(Modifier.ABSTRACT)) {
            error(formType, annotationName + " classes must be abstract");
            return null;
        }

        boolean failed = false;
        if (formType.getKind() == ElementKind.CLASS && !validateAbstractClassMembers(formType)) {
            failed = true;
        }
        List<FieldSpec> fields = new ArrayList<>();
        List<ComponentStateSpec> componentStates = new ArrayList<>();
        Set<String> fieldNames = new LinkedHashSet<>();
        Set<String> fieldIds = new LinkedHashSet<>();
        Set<String> stateNames = new LinkedHashSet<>();
        List<PendingStateDependency> pendingStateDependencies = new ArrayList<>();
        for (FieldSource source : formFieldSources(formType)) {
            if (source.abstractAccessor() && !validateAbstractAccessorShape(source.method())) {
                failed = true;
                continue;
            }
            boolean componentState = source.getAnnotation(ComponentState.class) != null;
            boolean textField = source.getAnnotation(TextField.class) != null;
            boolean checkBox = source.getAnnotation(CheckBox.class) != null;
            boolean comboBox = source.getAnnotation(ComboBox.class) != null;
            boolean radioGroup = source.getAnnotation(RadioGroup.class) != null;
            boolean dateField = source.getAnnotation(DateField.class) != null;
            int fieldKindCount = (textField ? 1 : 0)
                    + (checkBox ? 1 : 0)
                    + (comboBox ? 1 : 0)
                    + (radioGroup ? 1 : 0)
                    + (dateField ? 1 : 0);
            if (fieldKindCount == 0) {
                if (componentState && source.abstractAccessor()) {
                    if (!isComponentStateModel(source.type())) {
                        error(source.element(), "@CoruscoForm.ComponentState auxiliary accessors must return ComponentStateModel");
                        failed = true;
                        continue;
                    }
                    String stateConstantName = constantName(source.name());
                    ComponentStateSpec state = new ComponentStateSpec(stateConstantName, source.name(), true);
                    if (!stateNames.add(state.componentName) || fieldNames.contains(state.componentName)) {
                        error(source.element(), "Duplicate @CoruscoForm component state name in "
                                + formType.getSimpleName() + ": " + state.componentName);
                        failed = true;
                        continue;
                    }
                    componentStates.add(state);
                    pendingStateDependencies.add(new PendingStateDependency(source, stateConstantName, state.componentName));
                } else if (componentState) {
                    error(source.element(), "Record component @CoruscoForm.ComponentState must accompany a field kind annotation");
                    failed = true;
                } else if (hasDependencies(source.element())) {
                    error(source.element(), "@CoruscoForm.DependsOn requires @CoruscoForm.ComponentState");
                    failed = true;
                } else if (source.abstractAccessor()) {
                    error(source.element(), "Abstract @CoruscoForm accessor must have a field kind annotation");
                    failed = true;
                } else if (hasFieldMetadata(source.element())) {
                    error(source.element(), "Field metadata annotations require a field kind annotation");
                    failed = true;
                }
                continue;
            }
            if (fieldKindCount > 1) {
                error(source.element(), source.fieldKindConflictMessage());
                failed = true;
                continue;
            }
            if (checkBox && !isBoolean(source.type())) {
                error(source.element(), "@CheckBox requires boolean or java.lang.Boolean component type");
                failed = true;
                continue;
            }
            if (comboBox && source.type().getKind() != TypeKind.DECLARED) {
                error(source.element(), "@ComboBox requires a declared component type");
                failed = true;
                continue;
            }
            if (radioGroup && !isEnum(source.type())) {
                error(source.element(), "@RadioGroup requires an enum component type in this processor stage");
                failed = true;
                continue;
            }
            if (dateField && !isLocalDate(source.type())) {
                error(source.element(), "@DateField requires java.time.LocalDate component type");
                failed = true;
                continue;
            }
            if (!isSupportedValueType(source.type())) {
                error(source.element(), "Generated field keys require primitive or declared component types");
                failed = true;
                continue;
            }
            FieldSpec field = fieldSpec(formType, formId, source, textField, checkBox, comboBox,
                    radioGroup, dateField);
            if (!fieldNames.add(field.componentName)) {
                error(source.element(), "Duplicate @CoruscoForm field name in " + formType.getSimpleName() + ": "
                        + field.componentName);
                failed = true;
                continue;
            }
            if (stateNames.contains(field.componentName)) {
                error(source.element(), "Duplicate @CoruscoForm component state name in "
                        + formType.getSimpleName() + ": " + field.componentName);
                failed = true;
                continue;
            }
            if (!fieldIds.add(field.keyId)) {
                error(source.element(), "Duplicate @CoruscoForm field id in " + formType.getSimpleName() + ": "
                        + field.keyId);
                failed = true;
                continue;
            }
            if (!validateMetadata(source, field)) {
                failed = true;
                continue;
            }
            if (field.componentState) {
                ComponentStateSpec state = new ComponentStateSpec(
                        field.constantName + "_STATE",
                        field.componentName + "State"
                );
                if (!stateNames.add(state.componentName) || fieldNames.contains(state.componentName)) {
                    error(source.element(), "Duplicate @CoruscoForm component state name in "
                            + formType.getSimpleName() + ": " + state.componentName);
                    failed = true;
                    continue;
                }
                componentStates.add(state);
                pendingStateDependencies.add(new PendingStateDependency(source, state.constantName, state.componentName));
            } else if (hasDependencies(source.element())) {
                error(source.element(), "@CoruscoForm.DependsOn requires @CoruscoForm.ComponentState");
                failed = true;
                continue;
            }
            fields.add(field);
        }

        if (!failed && !pendingStateDependencies.isEmpty()) {
            componentStates = attachDependencies(formType, fields, componentStates, pendingStateDependencies);
            failed = componentStates == null;
        }

        if (fields.isEmpty() && !failed) {
            error(formType, formType.getKind() == ElementKind.RECORD
                    ? "@CoruscoForm record must contain at least one field kind annotation"
                    : "@CoruscoForm abstract class must contain at least one annotated abstract accessor");
            return null;
        }
        if (failed) {
            return null;
        }
        String sourceType = formType.getSimpleName().toString();
        String resultImplementationType = formType.getKind() == ElementKind.CLASS ? "Generated" + sourceType : null;
        if (resultImplementationType != null && generatedResultTypeExists(formType, resultImplementationType)) {
            error(formType, "Generated form result type would collide with existing type: " + resultImplementationType);
            return null;
        }
        return new FormSpec(
                formId,
                sourceType,
                resultImplementationType,
                fields,
                componentStates
        );
    }

    private List<FieldSource> formFieldSources(TypeElement formType) {
        if (formType.getKind() == ElementKind.RECORD) {
            List<FieldSource> result = new ArrayList<>();
            for (RecordComponentElement component : formType.getRecordComponents()) {
                result.add(FieldSource.recordComponent(component));
            }
            return result;
        }
        List<FieldSource> result = new ArrayList<>();
        for (Element enclosed : formType.getEnclosedElements()) {
            if (!(enclosed instanceof ExecutableElement method)) {
                continue;
            }
            if (!method.getModifiers().contains(Modifier.ABSTRACT)) {
                continue;
            }
            result.add(FieldSource.abstractAccessor(method));
        }
        return result;
    }

    private List<ComponentStateSpec> attachDependencies(
            TypeElement formType,
            List<FieldSpec> fields,
            List<ComponentStateSpec> componentStates,
            List<PendingStateDependency> pendingStateDependencies
    ) {
        java.util.Map<String, FieldSpec> fieldsByName = new java.util.LinkedHashMap<>();
        for (FieldSpec field : fields) {
            fieldsByName.put(field.componentName, field);
        }
        java.util.Map<String, java.util.List<DependencySpec>> dependenciesByState = new java.util.LinkedHashMap<>();
        boolean failed = false;
        for (PendingStateDependency pending : pendingStateDependencies) {
            java.util.List<DependencySpec> dependencies = dependencies(pending, fieldsByName, formType);
            if (dependencies == null) {
                failed = true;
            } else if (!dependencies.isEmpty()) {
                dependenciesByState.put(pending.stateName, dependencies);
            }
        }
        if (failed) {
            return null;
        }
        List<ComponentStateSpec> result = new ArrayList<>();
        for (ComponentStateSpec state : componentStates) {
            result.add(new ComponentStateSpec(
                    state.constantName,
                    state.componentName,
                    state.resultAccessor,
                    dependenciesByState.getOrDefault(state.componentName, List.of())
            ));
        }
        return result;
    }

    private List<DependencySpec> dependencies(
            PendingStateDependency pending,
            java.util.Map<String, FieldSpec> fieldsByName,
            TypeElement formType
    ) {
        DependsOn[] annotations = pending.source.element().getAnnotationsByType(DependsOn.class);
        if (annotations.length == 0) {
            return List.of();
        }
        List<DependencySpec> result = new ArrayList<>();
        boolean failed = false;
        int index = 0;
        for (DependsOn dependency : annotations) {
            if (dependency.field().isBlank()) {
                error(pending.source.element(), "@CoruscoForm.DependsOn field must not be blank");
                failed = true;
                continue;
            }
            FieldSpec sourceField = fieldsByName.get(dependency.field());
            if (sourceField == null) {
                error(pending.source.element(), "@CoruscoForm.DependsOn field does not match a generated form field in "
                        + formType.getSimpleName() + ": " + dependency.field());
                failed = true;
                continue;
            }
            if (dependency.values().length == 0) {
                error(pending.source.element(), "@CoruscoForm.DependsOn values must not be empty");
                failed = true;
                continue;
            }
            List<String> values = java.util.Arrays.stream(dependency.values())
                    .map(String::trim)
                    .toList();
            if (values.stream().anyMatch(String::isBlank)) {
                error(pending.source.element(), "@CoruscoForm.DependsOn values must not contain blank entries");
                failed = true;
                continue;
            }
            List<String> valueExpressions = dependencyValueExpressions(pending.source.element(), sourceField, values);
            if (valueExpressions == null) {
                failed = true;
                continue;
            }
            result.add(new DependencySpec(
                    pending.stateConstantName + "_DEPENDS_ON_" + sourceField.constantName
                            + (index == 0 ? "" : "_" + index),
                    sourceField.componentName,
                    sourceField.constantName,
                    sourceField.textField,
                    pending.stateName,
                    valueExpressions,
                    dependency.effect().name()
            ));
            index++;
        }
        return failed ? null : result;
    }

    private List<String> dependencyValueExpressions(Element dependencyElement, FieldSpec sourceField, List<String> values) {
        if (sourceField.textField) {
            return values.stream()
                    .map(FormProcessor::stringLiteral)
                    .toList();
        }
        if ("CHECK_BOX".equals(sourceField.kind)) {
            List<String> result = new ArrayList<>();
            boolean failed = false;
            for (String value : values) {
                if ("true".equals(value)) {
                    result.add("Boolean.TRUE");
                } else if ("false".equals(value)) {
                    result.add("Boolean.FALSE");
                } else {
                    error(dependencyElement, "@CoruscoForm.DependsOn value for checkbox field "
                            + sourceField.componentName + " must be true or false: " + value);
                    failed = true;
                }
            }
            return failed ? null : result;
        }
        if (!sourceField.enumOptionConstants.isEmpty()) {
            java.util.Map<String, String> constantsByToken = new java.util.LinkedHashMap<>();
            java.util.Set<String> ambiguousTokens = new java.util.LinkedHashSet<>();
            for (String enumConstant : sourceField.enumOptionConstants) {
                registerDependencyToken(constantsByToken, ambiguousTokens, enumConstant, enumConstant);
            }
            for (OptionSpec option : sourceField.options) {
                registerDependencyToken(constantsByToken, ambiguousTokens, option.key, option.enumConstantName);
            }
            List<String> result = new ArrayList<>();
            boolean failed = false;
            for (String value : values) {
                if (ambiguousTokens.contains(value)) {
                    error(dependencyElement, "@CoruscoForm.DependsOn value for enum field "
                            + sourceField.componentName + " is ambiguous: " + value);
                    failed = true;
                } else {
                    String enumConstant = constantsByToken.get(value);
                    if (enumConstant == null) {
                        error(dependencyElement, "@CoruscoForm.DependsOn value does not match an enum constant or option key for "
                                + sourceField.componentName + ": " + value);
                        failed = true;
                    } else {
                        result.add(sourceField.valueType + "." + enumConstant);
                    }
                }
            }
            return failed ? null : result;
        }
        error(dependencyElement, "@CoruscoForm.DependsOn does not support values for field "
                + sourceField.componentName + " of type " + sourceField.valueType);
        return null;
    }

    private static void registerDependencyToken(
            java.util.Map<String, String> constantsByToken,
            java.util.Set<String> ambiguousTokens,
            String token,
            String enumConstant
    ) {
        String previous = constantsByToken.putIfAbsent(token, enumConstant);
        if (previous != null && !previous.equals(enumConstant)) {
            ambiguousTokens.add(token);
        }
    }

    private boolean validateAbstractClassMembers(TypeElement formType) {
        boolean valid = true;
        for (Element enclosed : formType.getEnclosedElements()) {
            if (!(enclosed instanceof ExecutableElement method)) {
                continue;
            }
            if (method.getModifiers().contains(Modifier.ABSTRACT)) {
                continue;
            }
            if (hasFieldKind(method) || hasFieldMetadata(method)) {
                error(method, "@CoruscoForm field annotations on abstract classes require abstract accessor methods");
                valid = false;
            } else if (method.getAnnotation(ComponentState.class) != null) {
                error(method, "@CoruscoForm component state annotations on abstract classes require abstract accessors");
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateAbstractAccessorShape(ExecutableElement method) {
        boolean valid = true;
        if (!method.getParameters().isEmpty()) {
            error(method, "Abstract @CoruscoForm accessor must not declare parameters");
            valid = false;
        }
        if (!method.getTypeParameters().isEmpty()) {
            error(method, "Abstract @CoruscoForm accessor must not be generic");
            valid = false;
        }
        if (method.getReturnType().getKind() == TypeKind.VOID) {
            error(method, "Abstract @CoruscoForm accessor must return a value");
            valid = false;
        }
        return valid;
    }

    private boolean generatedResultTypeExists(TypeElement owner, String simpleName) {
        for (Element enclosed : owner.getEnclosedElements()) {
            if (enclosed instanceof TypeElement type && type.getSimpleName().contentEquals(simpleName)) {
                return true;
            }
        }
        String packageName = elements.getPackageOf(owner).getQualifiedName().toString();
        String qualifiedName = packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
        return elements.getTypeElement(qualifiedName) != null;
    }

    private FieldSpec fieldSpec(
            TypeElement formType,
            String formId,
            FieldSource source,
            boolean textField,
            boolean checkBox,
            boolean comboBox,
            boolean radioGroup,
            boolean dateField
    ) {
        String componentName = source.name();
        String constantName = constantName(componentName);
        String keyId = formId + "/" + kebab(componentName);
        String ownerType = formType.getSimpleName().toString();
        String accessorType = sourceType(source.type());
        String valueType = genericType(source.type());
        String valueClass = classLiteral(source.type());
        String kind = fieldKind(textField, checkBox, comboBox, radioGroup, dateField);
        boolean textKey = textField || dateField;
        String modelType = textKey ? "TextFieldModel" : "FieldModel";
        String converterExpression = converterExpression(source.type(), textField, dateField);
        String viewComponentType = viewComponentType(textField, checkBox, comboBox, radioGroup, dateField, valueType);
        String viewMethodName = viewMethodName(componentName, textField, checkBox, comboBox, radioGroup, dateField);
        List<OptionSpec> options = enumOptions(source, comboBox, radioGroup);
        List<String> enumOptionConstants = enumConstantNames(source.type());
        Help help = source.getAnnotation(Help.class);
        String tooltipId = null;
        String helpTopicId = null;
        if (help != null) {
            tooltipId = help.tooltip().isBlank() ? keyId + "/tooltip" : help.tooltip();
            helpTopicId = help.topic().isBlank() ? null : help.topic();
        }
        List<ConstraintSpec> constraints = constraints(source.element(), keyId, constantName);
        return new FieldSpec(
                constantName,
                keyId,
                componentName,
                ownerType,
                accessorType,
                valueType,
                valueClass,
                kind,
                constantName + "_LABEL",
                tooltipId == null ? null : constantName + "_TOOLTIP",
                tooltipId,
                helpTopicId,
                textKey,
                modelType,
                converterExpression,
                viewComponentType,
                viewMethodName,
                source.getAnnotation(ComponentState.class) != null,
                enumOptionConstants,
                options,
                constraints
        );
    }

    private List<OptionSpec> enumOptions(FieldSource source, boolean comboBox, boolean radioGroup) {
        if (!comboBox && !radioGroup) {
            return List.of();
        }
        ComboBox comboAnnotation = source.getAnnotation(ComboBox.class);
        RadioGroup radioAnnotation = source.getAnnotation(RadioGroup.class);
        if ((comboAnnotation != null && !comboAnnotation.enumOptions())
                || (radioAnnotation != null && !radioAnnotation.enumOptions())) {
            return List.of();
        }
        Element typeElement = types.asElement(source.type());
        if (typeElement == null || typeElement.getKind() != ElementKind.ENUM) {
            return List.of();
        }
        List<OptionWithOrder> options = new ArrayList<>();
        Set<String> keys = new LinkedHashSet<>();
        int declarationOrder = 0;
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.ENUM_CONSTANT) {
                VariableElement constant = (VariableElement) enclosed;
                Option option = constant.getAnnotation(Option.class);
                String enumConstantName = constant.getSimpleName().toString();
                String key = option == null || option.key().isBlank() ? enumOptionKey(enumConstantName) : option.key();
                if (!isStableId(key)) {
                    error(constant, "@CoruscoForm.Option key must contain only letters, digits, dots, underscores, dashes, or slashes");
                    continue;
                }
                if (!keys.add(key)) {
                    error(constant, "Duplicate option key in " + typeElement.getSimpleName() + ": " + key);
                    continue;
                }
                int order = option == null || option.order() < 0 ? declarationOrder : option.order();
                options.add(new OptionWithOrder(
                        new OptionSpec(enumConstantName, enumConstantName, key),
                        order,
                        declarationOrder
                ));
                declarationOrder++;
            }
        }
        options.sort(java.util.Comparator
                .comparingInt((OptionWithOrder option) -> option.order)
                .thenComparingInt(option -> option.declarationOrder));
        return options.stream()
                .map(option -> option.option)
                .toList();
    }

    private List<String> enumConstantNames(TypeMirror type) {
        Element typeElement = types.asElement(type);
        if (typeElement == null || typeElement.getKind() != ElementKind.ENUM) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.ENUM_CONSTANT) {
                result.add(enclosed.getSimpleName().toString());
            }
        }
        return result;
    }

    private boolean validateMetadata(FieldSource source, FieldSpec field) {
        Element element = source.element();
        TypeMirror type = source.type();
        boolean valid = true;
        if (element.getAnnotation(Length.class) != null && (!field.textField || !isString(type))) {
            error(element, "@Length is supported only on @TextField String components");
            valid = false;
        }
        if (element.getAnnotation(DecimalRange.class) != null && (!field.textField || !isBigDecimal(type))) {
            error(element, "@DecimalRange is supported only on @TextField BigDecimal components");
            valid = false;
        }
        if (field.textField && field.converterExpression == null) {
            error(element, "@TextField supports String, Integer, BigDecimal, and LocalDate in this processor stage");
            valid = false;
        }
        if (element.getAnnotation(IntRange.class) != null && (!field.textField || !isInteger(type))) {
            error(element, "@IntRange is supported only on @TextField integer components");
            valid = false;
        }
        if (element.getAnnotation(Regex.class) != null && (!field.textField || !isString(type))) {
            error(element, "@Regex is supported only on @TextField String components");
            valid = false;
        }
        Length length = element.getAnnotation(Length.class);
        if (length != null && (length.min() < 0 || length.max() < length.min())) {
            error(element, "@Length requires 0 <= min <= max");
            valid = false;
        }
        DecimalRange decimalRange = element.getAnnotation(DecimalRange.class);
        if (decimalRange != null && !validateDecimalRange(element, decimalRange)) {
            valid = false;
        }
        IntRange intRange = element.getAnnotation(IntRange.class);
        if (intRange != null && intRange.max() < intRange.min()) {
            error(element, "@IntRange requires min <= max");
            valid = false;
        }
        Regex regex = element.getAnnotation(Regex.class);
        if (regex != null && regex.value().isBlank()) {
            error(element, "@Regex pattern must not be blank");
            valid = false;
        }
        Help help = element.getAnnotation(Help.class);
        if (help != null) {
            if (!help.tooltip().isBlank() && !isStableId(help.tooltip())) {
                error(element, "@Help tooltip must contain only letters, digits, dots, underscores, dashes, or slashes");
                valid = false;
            }
            if (!help.topic().isBlank() && !isStableId(help.topic())) {
                error(element, "@Help topic must contain only letters, digits, dots, underscores, dashes, or slashes");
                valid = false;
            }
        }
        return valid;
    }

    private String converterExpression(TypeMirror type, boolean textField, boolean dateField) {
        if (dateField) {
            return "Converters.localDate(EmptyTextPolicy.NULL_VALUE)";
        }
        if (!textField) {
            return null;
        }
        String erased = types.erasure(type).toString();
        if (type.getKind() == TypeKind.INT) {
            return "Converters.integer(EmptyTextPolicy.NULL_VALUE)";
        }
        return switch (erased) {
            case "java.lang.String" -> "Converters.string()";
            case "java.lang.Integer" -> "Converters.integer(EmptyTextPolicy.NULL_VALUE)";
            case "java.math.BigDecimal" -> "Converters.bigDecimal(EmptyTextPolicy.NULL_VALUE)";
            case "java.time.LocalDate" -> "Converters.localDate(EmptyTextPolicy.NULL_VALUE)";
            default -> null;
        };
    }

    private List<ConstraintSpec> constraints(Element component, String keyId, String constantName) {
        List<ConstraintSpec> constraints = new ArrayList<>();
        if (component.getAnnotation(Required.class) != null) {
            constraints.add(new ConstraintSpec("REQUIRED", constantName + "_REQUIRED", keyId + "/required", null, null));
        }
        Length length = component.getAnnotation(Length.class);
        if (length != null) {
            constraints.add(new ConstraintSpec(
                    "LENGTH",
                    constantName + "_LENGTH",
                    keyId + "/length",
                    Integer.toString(length.min()),
                    Integer.toString(length.max())
            ));
        }
        DecimalRange decimalRange = component.getAnnotation(DecimalRange.class);
        if (decimalRange != null) {
            constraints.add(new ConstraintSpec(
                    "DECIMAL_RANGE",
                    constantName + "_DECIMAL_RANGE",
                    keyId + "/decimal-range",
                    emptyToNull(decimalRange.min()),
                    emptyToNull(decimalRange.max())
            ));
        }
        IntRange intRange = component.getAnnotation(IntRange.class);
        if (intRange != null) {
            constraints.add(new ConstraintSpec(
                    "INT_RANGE",
                    constantName + "_INT_RANGE",
                    keyId + "/int-range",
                    Integer.toString(intRange.min()),
                    Integer.toString(intRange.max())
            ));
        }
        Regex regex = component.getAnnotation(Regex.class);
        if (regex != null) {
            constraints.add(new ConstraintSpec(
                    "REGEX",
                    constantName + "_REGEX",
                    keyId + "/regex",
                    regex.value(),
                    null
            ));
        }
        return List.copyOf(constraints);
    }

    private boolean isBoolean(TypeMirror type) {
        if (type.getKind() == TypeKind.BOOLEAN) {
            return true;
        }
        return "java.lang.Boolean".equals(types.erasure(type).toString());
    }

    private boolean isString(TypeMirror type) {
        return "java.lang.String".equals(types.erasure(type).toString());
    }

    private boolean isBigDecimal(TypeMirror type) {
        return "java.math.BigDecimal".equals(types.erasure(type).toString());
    }

    private boolean isInteger(TypeMirror type) {
        return type.getKind() == TypeKind.INT || "java.lang.Integer".equals(types.erasure(type).toString());
    }

    private boolean isLocalDate(TypeMirror type) {
        return "java.time.LocalDate".equals(types.erasure(type).toString());
    }

    private boolean isComponentStateModel(TypeMirror type) {
        return "cz.auderis.corusco.core.form.ComponentStateModel".equals(types.erasure(type).toString());
    }

    private boolean isEnum(TypeMirror type) {
        Element element = types.asElement(type);
        return element != null && element.getKind() == ElementKind.ENUM;
    }

    private boolean validateDecimalRange(Element element, DecimalRange decimalRange) {
        boolean valid = true;
        if (decimalRange.min().isBlank() && decimalRange.max().isBlank()) {
            error(element, "@DecimalRange requires at least one bound");
            valid = false;
        }
        BigDecimal min = parseDecimal(element, decimalRange.min(), "min");
        BigDecimal max = parseDecimal(element, decimalRange.max(), "max");
        if (min != null && max != null && min.compareTo(max) > 0) {
            error(element, "@DecimalRange requires min <= max");
            valid = false;
        }
        return valid && (decimalRange.min().isBlank() || min != null) && (decimalRange.max().isBlank() || max != null);
    }

    private BigDecimal parseDecimal(Element element, String value, String label) {
        if (value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            error(element, "@DecimalRange " + label + " is not a valid decimal");
            return null;
        }
    }

    private static boolean isSupportedValueType(TypeMirror type) {
        return type.getKind().isPrimitive() || type.getKind() == TypeKind.DECLARED;
    }

    private static boolean hasFieldMetadata(Element element) {
        return element.getAnnotation(Required.class) != null
                || element.getAnnotation(Length.class) != null
                || element.getAnnotation(DecimalRange.class) != null
                || element.getAnnotation(IntRange.class) != null
                || element.getAnnotation(Regex.class) != null
                || element.getAnnotation(Help.class) != null;
    }

    private static boolean hasDependencies(Element element) {
        return element.getAnnotation(DependsOn.class) != null
                || element.getAnnotation(DependsOn.List.class) != null;
    }

    private static boolean hasFieldKind(Element element) {
        return element.getAnnotation(TextField.class) != null
                || element.getAnnotation(CheckBox.class) != null
                || element.getAnnotation(ComboBox.class) != null
                || element.getAnnotation(RadioGroup.class) != null
                || element.getAnnotation(DateField.class) != null;
    }

    private String genericType(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return types.boxedClass((PrimitiveType) type).getQualifiedName().toString();
        }
        return type.toString();
    }

    private String sourceType(TypeMirror type) {
        return type.toString();
    }

    private String classLiteral(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return types.boxedClass((PrimitiveType) type).getQualifiedName() + ".class";
        }
        return types.erasure(type).toString() + ".class";
    }

    private static String constantName(String componentName) {
        String kebab = kebab(componentName);
        return kebab.replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private static String fieldKind(
            boolean textField,
            boolean checkBox,
            boolean comboBox,
            boolean radioGroup,
            boolean dateField
    ) {
        if (textField) {
            return "TEXT";
        }
        if (checkBox) {
            return "CHECK_BOX";
        }
        if (comboBox) {
            return "COMBO_BOX";
        }
        if (radioGroup) {
            return "RADIO_GROUP";
        }
        if (dateField) {
            return "DATE";
        }
        throw new IllegalArgumentException("No field kind selected");
    }

    private static String viewComponentType(
            boolean textField,
            boolean checkBox,
            boolean comboBox,
            boolean radioGroup,
            boolean dateField,
            String valueType
    ) {
        if (textField || dateField) {
            return "JTextField";
        }
        if (checkBox) {
            return "JCheckBox";
        }
        if (comboBox) {
            return "JComboBox<" + valueType + ">";
        }
        if (radioGroup) {
            return "JComponent";
        }
        throw new IllegalArgumentException("No field kind selected");
    }

    private static String viewMethodName(
            String componentName,
            boolean textField,
            boolean checkBox,
            boolean comboBox,
            boolean radioGroup,
            boolean dateField
    ) {
        if (textField || dateField) {
            return componentName + "Field";
        }
        if (checkBox) {
            return componentName + "Box";
        }
        if (comboBox) {
            return componentName + "Combo";
        }
        if (radioGroup) {
            return componentName + "Group";
        }
        throw new IllegalArgumentException("No field kind selected");
    }

    private record OptionWithOrder(OptionSpec option, int order, int declarationOrder) {
    }

    private static boolean isStableId(String value) {
        return value.matches("[A-Za-z0-9][A-Za-z0-9._/-]*");
    }

    private static String stringLiteral(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String kebab(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (!result.isEmpty()) {
                    result.append('-');
                }
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private static String enumOptionKey(String enumConstantName) {
        if (enumConstantName.equals(enumConstantName.toUpperCase(Locale.ROOT))) {
            return enumConstantName.toLowerCase(Locale.ROOT).replace('_', '-');
        }
        return kebab(enumConstantName);
    }

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private GeneratedSourceWriter sourceWriter() {
        return new GeneratedSourceWriter(elements, filer, messager);
    }

    private static String emptyToNull(String value) {
        return value.isBlank() ? null : value;
    }

    private record FieldSource(
            Element element,
            String name,
            TypeMirror type,
            boolean abstractAccessor
    ) {

        static FieldSource recordComponent(RecordComponentElement component) {
            return new FieldSource(component, component.getSimpleName().toString(), component.asType(), false);
        }

        static FieldSource abstractAccessor(ExecutableElement method) {
            return new FieldSource(method, method.getSimpleName().toString(), method.getReturnType(), true);
        }

        <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annotationType) {
            return element.getAnnotation(annotationType);
        }

        ExecutableElement method() {
            return (ExecutableElement) element;
        }

        String fieldKindConflictMessage() {
            return abstractAccessor
                    ? "Abstract accessor must have only one field kind annotation"
                    : "Record component must have only one field kind annotation";
        }
    }

    private record PendingStateDependency(
            FieldSource source,
            String stateConstantName,
            String stateName
    ) {
    }
}
