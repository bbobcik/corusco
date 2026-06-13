package cz.auderis.corusco.processor;

import cz.auderis.corusco.annotations.CheckBox;
import cz.auderis.corusco.annotations.ComboBox;
import cz.auderis.corusco.annotations.Column;
import cz.auderis.corusco.annotations.DecimalRange;
import cz.auderis.corusco.annotations.DateField;
import cz.auderis.corusco.annotations.Help;
import cz.auderis.corusco.annotations.IntRange;
import cz.auderis.corusco.annotations.Length;
import cz.auderis.corusco.annotations.Required;
import cz.auderis.corusco.annotations.Regex;
import cz.auderis.corusco.annotations.SwingForm;
import cz.auderis.corusco.annotations.SwingTable;
import cz.auderis.corusco.annotations.TextField;
import cz.auderis.corusco.annotations.UiAction;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Initial Corusco annotation processor.
 *
 * <p>This first slice generates typed field-key classes for annotated records.
 * It deliberately uses only {@code javax.lang.model} APIs; no runtime
 * reflection or annotation scanning is involved.</p>
 */
@SupportedAnnotationTypes({
        "cz.auderis.corusco.annotations.SwingForm",
        "cz.auderis.corusco.annotations.SwingTable",
        "cz.auderis.corusco.annotations.UiAction"
})
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public final class CoruscoAnnotationProcessor extends AbstractProcessor {

    private Elements elements;
    private Types types;
    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elements = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(SwingForm.class)) {
            if (element instanceof TypeElement typeElement) {
                processForm(typeElement);
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(SwingTable.class)) {
            if (element instanceof TypeElement typeElement) {
                processTable(typeElement);
            }
        }
        processActions(roundEnv);
        return true;
    }

    private void processTable(TypeElement tableType) {
        SwingTable annotation = tableType.getAnnotation(SwingTable.class);
        if (tableType.getKind() != ElementKind.RECORD) {
            error(tableType, "@SwingTable is supported only on records");
            return;
        }
        if (annotation.id().isBlank()) {
            error(tableType, "@SwingTable id must not be blank");
            return;
        }
        if (!isStableId(annotation.id())) {
            error(tableType, "@SwingTable id must contain only letters, digits, dots, underscores, dashes, or slashes");
            return;
        }
        if (!tableType.getTypeParameters().isEmpty()) {
            error(tableType, "@SwingTable generic records are not supported by this processor stage");
            return;
        }

        List<TableComponentSpec> components = new ArrayList<>();
        for (RecordComponentElement component : tableType.getRecordComponents()) {
            components.add(new TableComponentSpec(component.getSimpleName().toString()));
        }
        List<TableColumnSpec> columns = new ArrayList<>();
        Set<String> ids = new java.util.LinkedHashSet<>();
        boolean failed = false;
        for (RecordComponentElement component : tableType.getRecordComponents()) {
            Column annotationColumn = component.getAnnotation(Column.class);
            if (annotationColumn == null) {
                continue;
            }
            if (!isSupportedValueType(component.asType())) {
                error(component, "@Column requires primitive or declared component types");
                failed = true;
                continue;
            }
            String id = annotationColumn.id().isBlank()
                    ? annotation.id() + "/" + kebab(component.getSimpleName().toString())
                    : annotationColumn.id();
            if (!isStableId(id)) {
                error(component, "@Column id must contain only letters, digits, dots, underscores, dashes, or slashes");
                failed = true;
                continue;
            }
            if (!ids.add(id)) {
                error(component, "Duplicate @Column id in " + tableType.getSimpleName() + ": " + id);
                failed = true;
                continue;
            }
            if (annotationColumn.width() <= 0) {
                error(component, "@Column width must be greater than zero");
                failed = true;
                continue;
            }
            if (!annotationColumn.header().isBlank() && !isStableId(annotationColumn.header())) {
                error(component, "@Column header must contain only letters, digits, dots, underscores, dashes, or slashes");
                failed = true;
                continue;
            }
            if (!annotationColumn.tooltip().isBlank() && !isStableId(annotationColumn.tooltip())) {
                error(component, "@Column tooltip must contain only letters, digits, dots, underscores, dashes, or slashes");
                failed = true;
                continue;
            }
            columns.add(tableColumnSpec(tableType, component, annotationColumn, id, columns.size()));
        }
        if (columns.isEmpty() && !failed) {
            error(tableType, "@SwingTable record must contain at least one @Column component");
            return;
        }
        if (failed) {
            return;
        }
        new TableSourceWriter(elements, filer, messager)
                .writeTableSources(tableType, new TableSpec(
                        annotation.id(),
                        tableType.getSimpleName().toString(),
                        components,
                        columns
                ));
    }

    private TableColumnSpec tableColumnSpec(
            TypeElement tableType,
            RecordComponentElement component,
            Column column,
            String keyId,
            int componentOrder
    ) {
        String componentName = component.getSimpleName().toString();
        String constantName = constantName(componentName);
        String headerId = column.header().isBlank() ? keyId + "/header" : column.header();
        String tooltipId = column.tooltip().isBlank() ? null : column.tooltip();
        int order = column.order() < 0 ? componentOrder : column.order();
        return new TableColumnSpec(
                constantName,
                keyId,
                componentName,
                tableType.getSimpleName().toString(),
                genericType(component.asType()),
                classLiteral(component.asType()),
                constantName + "_HEADER",
                headerId,
                tooltipId == null ? null : constantName + "_TOOLTIP",
                tooltipId,
                column.width(),
                order,
                column.visible(),
                column.sortable(),
                column.filterable(),
                column.hideable(),
                column.editable()
        );
    }

    private void processActions(RoundEnvironment roundEnv) {
        Map<TypeElement, List<ActionSpec>> actionsByOwner = new LinkedHashMap<>();
        boolean failed = false;
        for (Element element : roundEnv.getElementsAnnotatedWith(UiAction.class)) {
            if (!(element instanceof ExecutableElement method)) {
                error(element, "@UiAction is supported only on methods");
                failed = true;
                continue;
            }
            Element owner = method.getEnclosingElement();
            if (!(owner instanceof TypeElement ownerType)) {
                error(method, "@UiAction method must be enclosed by a type");
                failed = true;
                continue;
            }
            ActionSpec action = actionSpec(method);
            if (action == null) {
                failed = true;
                continue;
            }
            List<ActionSpec> actions = actionsByOwner.computeIfAbsent(ownerType, ignored -> new ArrayList<>());
            if (actions.stream().anyMatch(existing -> existing.id.equals(action.id))) {
                error(method, "Duplicate @UiAction id in " + ownerType.getSimpleName() + ": " + action.id);
                failed = true;
                continue;
            }
            actions.add(action);
        }
        if (failed) {
            return;
        }
        for (Map.Entry<TypeElement, List<ActionSpec>> entry : actionsByOwner.entrySet()) {
            sourceWriter().writeActionsClass(entry.getKey(), entry.getValue());
        }
    }

    private ActionSpec actionSpec(ExecutableElement method) {
        UiAction annotation = method.getAnnotation(UiAction.class);
        if (!method.getParameters().isEmpty()) {
            error(method, "@UiAction methods must not declare parameters");
            return null;
        }
        if (method.getReturnType().getKind() != TypeKind.VOID) {
            error(method, "@UiAction methods must return void");
            return null;
        }
        if (annotation.id().isBlank()) {
            error(method, "@UiAction id must not be blank");
            return null;
        }
        if (!isStableId(annotation.id())) {
            error(method, "@UiAction id must contain only letters, digits, dots, underscores, dashes, or slashes");
            return null;
        }
        if (!annotation.text().isBlank() && !isStableId(annotation.text())) {
            error(method, "@UiAction text must contain only letters, digits, dots, underscores, dashes, or slashes");
            return null;
        }
        if (!annotation.tooltip().isBlank() && !isStableId(annotation.tooltip())) {
            error(method, "@UiAction tooltip must contain only letters, digits, dots, underscores, dashes, or slashes");
            return null;
        }
        if (annotation.acceleratorKey() == 0 && annotation.acceleratorModifiers() != 0) {
            error(method, "@UiAction acceleratorModifiers require acceleratorKey");
            return null;
        }
        String methodName = method.getSimpleName().toString();
        String constantName = constantName(methodName);
        String textId = annotation.text().isBlank() ? annotation.id() + "/text" : annotation.text();
        String tooltipId = annotation.tooltip().isBlank() ? null : annotation.tooltip();
        return new ActionSpec(
                constantName,
                annotation.id(),
                textId,
                tooltipId,
                annotation.mnemonic(),
                annotation.acceleratorKey(),
                annotation.acceleratorModifiers(),
                annotation.selectable()
        );
    }

    private void processForm(TypeElement formType) {
        SwingForm annotation = formType.getAnnotation(SwingForm.class);
        if (formType.getKind() != ElementKind.RECORD) {
            error(formType, "@SwingForm is supported only on records");
            return;
        }
        if (annotation.id().isBlank()) {
            error(formType, "@SwingForm id must not be blank");
            return;
        }
        if (!isStableId(annotation.id())) {
            error(formType, "@SwingForm id must contain only letters, digits, dots, underscores, dashes, or slashes");
            return;
        }
        if (!formType.getTypeParameters().isEmpty()) {
            error(formType, "@SwingForm generic records are not supported by this processor stage");
            return;
        }

        List<FieldSpec> fields = new ArrayList<>();
        boolean failed = false;
        for (RecordComponentElement component : formType.getRecordComponents()) {
            boolean textField = component.getAnnotation(TextField.class) != null;
            boolean checkBox = component.getAnnotation(CheckBox.class) != null;
            boolean comboBox = component.getAnnotation(ComboBox.class) != null;
            boolean dateField = component.getAnnotation(DateField.class) != null;
            int fieldKindCount = (textField ? 1 : 0)
                    + (checkBox ? 1 : 0)
                    + (comboBox ? 1 : 0)
                    + (dateField ? 1 : 0);
            if (fieldKindCount == 0) {
                if (hasFieldMetadata(component)) {
                    error(component, "Field metadata annotations require a field kind annotation");
                    failed = true;
                }
                continue;
            }
            if (fieldKindCount > 1) {
                error(component, "Record component must have only one field kind annotation");
                failed = true;
                continue;
            }
            if (checkBox && !isBoolean(component.asType())) {
                error(component, "@CheckBox requires boolean or java.lang.Boolean component type");
                failed = true;
                continue;
            }
            if (comboBox && component.asType().getKind() != TypeKind.DECLARED) {
                error(component, "@ComboBox requires a declared component type");
                failed = true;
                continue;
            }
            if (dateField && !isLocalDate(component.asType())) {
                error(component, "@DateField requires java.time.LocalDate component type");
                failed = true;
                continue;
            }
            if (!isSupportedValueType(component.asType())) {
                error(component, "Generated field keys require primitive or declared component types");
                failed = true;
                continue;
            }
            FieldSpec field = fieldSpec(formType, annotation.id(), component, textField, checkBox, comboBox, dateField);
            if (!validateMetadata(component, field)) {
                failed = true;
                continue;
            }
            fields.add(field);
        }

        if (fields.isEmpty() && !failed) {
            error(formType, "@SwingForm record must contain at least one @TextField or @CheckBox component");
            return;
        }
        if (failed) {
            return;
        }
        sourceWriter().writeFormSources(formType, fields);
    }

    private FieldSpec fieldSpec(
            TypeElement formType,
            String formId,
            RecordComponentElement component,
            boolean textField,
            boolean checkBox,
            boolean comboBox,
            boolean dateField
    ) {
        String componentName = component.getSimpleName().toString();
        String constantName = constantName(componentName);
        String keyId = formId + "/" + kebab(componentName);
        String ownerType = formType.getSimpleName().toString();
        String valueType = genericType(component.asType());
        String valueClass = classLiteral(component.asType());
        String kind = fieldKind(textField, checkBox, comboBox, dateField);
        boolean textKey = textField || dateField;
        String modelType = textKey ? "TextFieldModel" : "FieldModel";
        String converterExpression = converterExpression(component.asType(), textField, dateField);
        String viewComponentType = viewComponentType(textField, checkBox, comboBox, dateField, valueType);
        String viewMethodName = viewMethodName(componentName, textField, checkBox, comboBox, dateField);
        Help help = component.getAnnotation(Help.class);
        String tooltipId = null;
        String helpTopicId = null;
        if (help != null) {
            tooltipId = help.tooltip().isBlank() ? keyId + "/tooltip" : help.tooltip();
            helpTopicId = help.topic().isBlank() ? null : help.topic();
        }
        List<ConstraintSpec> constraints = constraints(component, keyId, constantName);
        return new FieldSpec(
                constantName,
                keyId,
                componentName,
                ownerType,
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
                constraints
        );
    }

    private boolean validateMetadata(RecordComponentElement component, FieldSpec field) {
        boolean valid = true;
        if (component.getAnnotation(Length.class) != null && (!field.textField || !isString(component.asType()))) {
            error(component, "@Length is supported only on @TextField String components");
            valid = false;
        }
        if (component.getAnnotation(DecimalRange.class) != null && (!field.textField || !isBigDecimal(component.asType()))) {
            error(component, "@DecimalRange is supported only on @TextField BigDecimal components");
            valid = false;
        }
        if (field.textField && field.converterExpression == null) {
            error(component, "@TextField supports String, Integer, BigDecimal, and LocalDate in this processor stage");
            valid = false;
        }
        if (component.getAnnotation(IntRange.class) != null && (!field.textField || !isInteger(component.asType()))) {
            error(component, "@IntRange is supported only on @TextField integer components");
            valid = false;
        }
        if (component.getAnnotation(Regex.class) != null && (!field.textField || !isString(component.asType()))) {
            error(component, "@Regex is supported only on @TextField String components");
            valid = false;
        }
        Length length = component.getAnnotation(Length.class);
        if (length != null && (length.min() < 0 || length.max() < length.min())) {
            error(component, "@Length requires 0 <= min <= max");
            valid = false;
        }
        DecimalRange decimalRange = component.getAnnotation(DecimalRange.class);
        if (decimalRange != null && !validateDecimalRange(component, decimalRange)) {
            valid = false;
        }
        IntRange intRange = component.getAnnotation(IntRange.class);
        if (intRange != null && intRange.max() < intRange.min()) {
            error(component, "@IntRange requires min <= max");
            valid = false;
        }
        Regex regex = component.getAnnotation(Regex.class);
        if (regex != null && regex.value().isBlank()) {
            error(component, "@Regex pattern must not be blank");
            valid = false;
        }
        Help help = component.getAnnotation(Help.class);
        if (help != null) {
            if (!help.tooltip().isBlank() && !isStableId(help.tooltip())) {
                error(component, "@Help tooltip must contain only letters, digits, dots, underscores, dashes, or slashes");
                valid = false;
            }
            if (!help.topic().isBlank() && !isStableId(help.topic())) {
                error(component, "@Help topic must contain only letters, digits, dots, underscores, dashes, or slashes");
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

    private List<ConstraintSpec> constraints(RecordComponentElement component, String keyId, String constantName) {
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

    private boolean validateDecimalRange(RecordComponentElement component, DecimalRange decimalRange) {
        boolean valid = true;
        if (decimalRange.min().isBlank() && decimalRange.max().isBlank()) {
            error(component, "@DecimalRange requires at least one bound");
            valid = false;
        }
        BigDecimal min = parseDecimal(component, decimalRange.min(), "min");
        BigDecimal max = parseDecimal(component, decimalRange.max(), "max");
        if (min != null && max != null && min.compareTo(max) > 0) {
            error(component, "@DecimalRange requires min <= max");
            valid = false;
        }
        return valid && (decimalRange.min().isBlank() || min != null) && (decimalRange.max().isBlank() || max != null);
    }

    private BigDecimal parseDecimal(RecordComponentElement component, String value, String label) {
        if (value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            error(component, "@DecimalRange " + label + " is not a valid decimal");
            return null;
        }
    }

    private static boolean isSupportedValueType(TypeMirror type) {
        return type.getKind().isPrimitive() || type.getKind() == TypeKind.DECLARED;
    }

    private static boolean hasFieldMetadata(RecordComponentElement component) {
        return component.getAnnotation(Required.class) != null
                || component.getAnnotation(Length.class) != null
                || component.getAnnotation(DecimalRange.class) != null
                || component.getAnnotation(IntRange.class) != null
                || component.getAnnotation(Regex.class) != null
                || component.getAnnotation(Help.class) != null;
    }

    private String genericType(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return types.boxedClass((PrimitiveType) type).getQualifiedName().toString();
        }
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

    private static String fieldKind(boolean textField, boolean checkBox, boolean comboBox, boolean dateField) {
        if (textField) {
            return "TEXT";
        }
        if (checkBox) {
            return "CHECK_BOX";
        }
        if (comboBox) {
            return "COMBO_BOX";
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
        throw new IllegalArgumentException("No field kind selected");
    }

    private static String viewMethodName(
            String componentName,
            boolean textField,
            boolean checkBox,
            boolean comboBox,
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
        throw new IllegalArgumentException("No field kind selected");
    }

    private static boolean isStableId(String value) {
        return value.matches("[A-Za-z0-9][A-Za-z0-9._/-]*");
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

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private GeneratedSourceWriter sourceWriter() {
        return new GeneratedSourceWriter(elements, filer, messager);
    }

    private static String emptyToNull(String value) {
        return value.isBlank() ? null : value;
    }
}
