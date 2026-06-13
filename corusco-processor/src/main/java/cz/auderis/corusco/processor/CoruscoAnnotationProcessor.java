package cz.auderis.corusco.processor;

import cz.auderis.corusco.annotations.CheckBox;
import cz.auderis.corusco.annotations.ComboBox;
import cz.auderis.corusco.annotations.DecimalRange;
import cz.auderis.corusco.annotations.DateField;
import cz.auderis.corusco.annotations.Help;
import cz.auderis.corusco.annotations.IntRange;
import cz.auderis.corusco.annotations.Length;
import cz.auderis.corusco.annotations.Required;
import cz.auderis.corusco.annotations.Regex;
import cz.auderis.corusco.annotations.SwingForm;
import cz.auderis.corusco.annotations.TextField;
import cz.auderis.corusco.annotations.UiAction;
import java.io.IOException;
import java.io.Writer;
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
import javax.tools.JavaFileObject;

/**
 * Initial Corusco annotation processor.
 *
 * <p>This first slice generates typed field-key classes for annotated records.
 * It deliberately uses only {@code javax.lang.model} APIs; no runtime
 * reflection or annotation scanning is involved.</p>
 */
@SupportedAnnotationTypes({
        "cz.auderis.corusco.annotations.SwingForm",
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
        processActions(roundEnv);
        return true;
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
            writeActionsClass(entry.getKey(), entry.getValue());
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
        writeFieldsClass(formType, fields);
        writeResourcesClass(formType, fields);
        writeProblemsClass(formType, fields);
        writeDescriptorsClass(formType, fields);
        writeFormModelClass(formType, fields);
        writeViewClass(formType, fields);
        writeBehaviorPlanClass(formType, fields);
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

    private void writeFieldsClass(TypeElement formType, List<FieldSpec> fields) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = formType.getSimpleName().toString();
        String generatedType = ownerType + "Fields";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        try {
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedName, formType);
            try (Writer writer = sourceFile.openWriter()) {
                if (!packageName.isEmpty()) {
                    writer.write("package " + packageName + ";\n\n");
                }
                writer.write("import cz.auderis.corusco.core.key.FieldKey;\n");
                writer.write("import cz.auderis.corusco.core.key.TextFieldKey;\n\n");
                writer.write("/**\n");
                writer.write(" * Generated field keys for {@link " + ownerType + "}.\n");
                writer.write(" */\n");
                writer.write("public final class " + generatedType + " {\n\n");
                writer.write("    private " + generatedType + "() {\n");
                writer.write("        throw new AssertionError(\"No instances\");\n");
                writer.write("    }\n\n");
                for (FieldSpec field : fields) {
                    writeField(writer, field);
                }
                writer.write("}\n");
            }
        } catch (IOException e) {
            error(formType, "Could not write generated field keys: " + e.getMessage());
        }
    }

    private void writeField(Writer writer, FieldSpec field) throws IOException {
        String keyType = field.textField ? "TextFieldKey" : "FieldKey";
        String factory = field.textField ? "TextFieldKey" : "FieldKey";
        writer.write("    /**\n");
        writer.write("     * Field key for {@code " + field.keyId + "}.\n");
        writer.write("     */\n");
        writer.write("    public static final " + keyType + "<" + field.ownerType + ", " + field.valueType + "> "
                + field.constantName + " =\n");
        writer.write("            " + factory + ".of(" + stringLiteralOrNull(field.keyId) + ", "
                + field.ownerType + ".class, " + field.valueClass + ");\n\n");
    }

    private void writeResourcesClass(TypeElement formType, List<FieldSpec> fields) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = formType.getSimpleName().toString();
        String generatedType = ownerType + "Resources";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        try {
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedName, formType);
            try (Writer writer = sourceFile.openWriter()) {
                writePackage(writer, packageName);
                writer.write("import cz.auderis.corusco.core.key.ResourceKey;\n\n");
                writer.write("/**\n");
                writer.write(" * Generated resource keys for {@link " + ownerType + "}.\n");
                writer.write(" */\n");
                writer.write("public final class " + generatedType + " {\n\n");
                writePrivateConstructor(writer, generatedType);
                for (FieldSpec field : fields) {
                    writeResourceKey(writer, field.labelConstant, field.keyId + "/label");
                    if (field.tooltipConstant != null) {
                        writeResourceKey(writer, field.tooltipConstant, field.tooltipId);
                    }
                }
                writer.write("}\n");
            }
        } catch (IOException e) {
            error(formType, "Could not write generated resource keys: " + e.getMessage());
        }
    }

    private void writeProblemsClass(TypeElement formType, List<FieldSpec> fields) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = formType.getSimpleName().toString();
        String generatedType = ownerType + "Problems";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        try {
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedName, formType);
            try (Writer writer = sourceFile.openWriter()) {
                writePackage(writer, packageName);
                writer.write("import cz.auderis.corusco.core.problem.ProblemCode;\n\n");
                writer.write("/**\n");
                writer.write(" * Generated problem codes for {@link " + ownerType + "}.\n");
                writer.write(" */\n");
                writer.write("public final class " + generatedType + " {\n\n");
                writePrivateConstructor(writer, generatedType);
                for (FieldSpec field : fields) {
                    for (ConstraintSpec constraint : field.constraints) {
                        writer.write("    /**\n");
                        writer.write("     * Problem code for {@code " + constraint.problemId + "}.\n");
                        writer.write("     */\n");
                        writer.write("    public static final ProblemCode " + constraint.problemConstant + " =\n");
                        writer.write("            ProblemCode.of(" + stringLiteralOrNull(constraint.problemId) + ");\n\n");
                    }
                }
                writer.write("}\n");
            }
        } catch (IOException e) {
            error(formType, "Could not write generated problem codes: " + e.getMessage());
        }
    }

    private void writeDescriptorsClass(TypeElement formType, List<FieldSpec> fields) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = formType.getSimpleName().toString();
        String generatedType = ownerType + "Descriptors";
        String resourcesType = ownerType + "Resources";
        String problemsType = ownerType + "Problems";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        try {
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedName, formType);
            try (Writer writer = sourceFile.openWriter()) {
                writePackage(writer, packageName);
                writer.write("import cz.auderis.corusco.core.key.HelpTopic;\n");
                writer.write("import cz.auderis.corusco.core.meta.ConstraintDescriptor;\n");
                writer.write("import cz.auderis.corusco.core.meta.FieldDescriptor;\n");
                writer.write("import cz.auderis.corusco.core.meta.FieldKind;\n");
                writer.write("import java.util.List;\n\n");
                writer.write("/**\n");
                writer.write(" * Generated field descriptors for {@link " + ownerType + "}.\n");
                writer.write(" */\n");
                writer.write("public final class " + generatedType + " {\n\n");
                writePrivateConstructor(writer, generatedType);
                for (FieldSpec field : fields) {
                    writeDescriptor(writer, field, resourcesType, problemsType);
                }
                writer.write("}\n");
            }
        } catch (IOException e) {
            error(formType, "Could not write generated field descriptors: " + e.getMessage());
        }
    }

    private void writeActionsClass(TypeElement ownerType, List<ActionSpec> actions) {
        String packageName = elements.getPackageOf(ownerType).getQualifiedName().toString();
        String ownerName = ownerType.getSimpleName().toString();
        String generatedType = ownerName + "Actions";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        try {
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedName, ownerType);
            try (Writer writer = sourceFile.openWriter()) {
                writePackage(writer, packageName);
                writer.write("import cz.auderis.corusco.core.command.AcceleratorDescriptor;\n");
                writer.write("import cz.auderis.corusco.core.command.ActionDescriptor;\n");
                writer.write("import cz.auderis.corusco.core.key.ActionKey;\n");
                writer.write("import cz.auderis.corusco.core.key.ResourceKey;\n\n");
                writer.write("/**\n");
                writer.write(" * Generated action descriptors for {@link " + ownerName + "}.\n");
                writer.write(" */\n");
                writer.write("public final class " + generatedType + " {\n\n");
                writePrivateConstructor(writer, generatedType);
                for (ActionSpec action : actions) {
                    writeAction(writer, action);
                }
                writer.write("}\n");
            }
        } catch (IOException e) {
            error(ownerType, "Could not write generated action descriptors: " + e.getMessage());
        }
    }

    private void writeFormModelClass(TypeElement formType, List<FieldSpec> fields) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = formType.getSimpleName().toString();
        String generatedType = ownerType + "FormModel";
        String fieldsType = ownerType + "Fields";
        String descriptorsType = ownerType + "Descriptors";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        try {
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedName, formType);
            try (Writer writer = sourceFile.openWriter()) {
                writePackage(writer, packageName);
                writer.write("import cz.auderis.corusco.core.convert.Converters;\n");
                writer.write("import cz.auderis.corusco.core.convert.EmptyTextPolicy;\n");
                writer.write("import cz.auderis.corusco.core.form.AbstractFormModel;\n");
                writer.write("import cz.auderis.corusco.core.form.FieldModel;\n");
                writer.write("import cz.auderis.corusco.core.form.TextFieldModel;\n");
                writer.write("import cz.auderis.corusco.core.meta.FieldDescriptor;\n");
                writer.write("import cz.auderis.corusco.core.problem.ProblemSet;\n");
                writer.write("import cz.auderis.corusco.core.validation.RuleSet;\n");
                writer.write("import cz.auderis.corusco.core.validation.Validators;\n");
                writer.write("import java.math.BigDecimal;\n");
                writer.write("import java.util.List;\n");
                writer.write("import java.util.regex.Pattern;\n\n");
                writer.write("/**\n");
                writer.write(" * Generated form model for {@link " + ownerType + "}.\n");
                writer.write(" */\n");
                writer.write("public final class " + generatedType + " extends AbstractFormModel<" + ownerType + "> {\n\n");
                for (FieldSpec field : fields) {
                    writer.write("    /**\n");
                    writer.write("     * Field model for {@code " + field.keyId + "}.\n");
                    writer.write("     */\n");
                    writer.write("    public final " + field.modelType + "<" + field.ownerType + ", " + field.valueType + "> "
                            + field.componentName + ";\n\n");
                }
                writer.write("    private final RuleSet<" + generatedType + "> rules;\n\n");
                writer.write("    /**\n");
                writer.write("     * Creates a generated form model.\n");
                writer.write("     *\n");
                writer.write("     * @param original original immutable record\n");
                writer.write("     */\n");
                writer.write("    public " + generatedType + "(" + ownerType + " original) {\n");
                writer.write("        java.util.Objects.requireNonNull(original, \"original\");\n");
                for (FieldSpec field : fields) {
                    writeFieldModelInitialization(writer, field, fieldsType);
                }
                writer.write("        this.rules = buildRules();\n");
                writer.write("    }\n\n");
                writeDescriptorList(writer, fields, descriptorsType);
                writeBuildRules(writer, fields, generatedType, fieldsType);
                writer.write("    @Override\n");
                writer.write("    protected ProblemSet validationProblems() {\n");
                writer.write("        return rules.validateAll(this);\n");
                writer.write("    }\n\n");
                writer.write("    @Override\n");
                writer.write("    protected " + ownerType + " createResult() {\n");
                writer.write("        return new " + ownerType + "(\n");
                for (int i = 0; i < fields.size(); i++) {
                    FieldSpec field = fields.get(i);
                    String suffix = i == fields.size() - 1 ? "\n" : ",\n";
                    writer.write("                " + resultValueExpression(field) + suffix);
                }
                writer.write("        );\n");
                writer.write("    }\n");
                writer.write("}\n");
            }
        } catch (IOException e) {
            error(formType, "Could not write generated form model: " + e.getMessage());
        }
    }

    private void writeViewClass(TypeElement formType, List<FieldSpec> fields) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = formType.getSimpleName().toString();
        String generatedType = ownerType + "View";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        try {
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedName, formType);
            try (Writer writer = sourceFile.openWriter()) {
                writePackage(writer, packageName);
                writer.write("import javax.swing.JCheckBox;\n");
                writer.write("import javax.swing.JComboBox;\n");
                writer.write("import javax.swing.JTextField;\n\n");
                writer.write("/**\n");
                writer.write(" * Generated Swing view contract for {@link " + ownerType + "}.\n");
                writer.write(" */\n");
                writer.write("public interface " + generatedType + " {\n\n");
                for (FieldSpec field : fields) {
                    writer.write("    /**\n");
                    writer.write("     * Returns the Swing component for {@code " + field.keyId + "}.\n");
                    writer.write("     *\n");
                    writer.write("     * @return component\n");
                    writer.write("     */\n");
                    writer.write("    " + field.viewComponentType + " " + field.viewMethodName + "();\n\n");
                }
                writer.write("}\n");
            }
        } catch (IOException e) {
            error(formType, "Could not write generated view contract: " + e.getMessage());
        }
    }

    private void writeBehaviorPlanClass(TypeElement formType, List<FieldSpec> fields) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = formType.getSimpleName().toString();
        String generatedType = ownerType + "BehaviorPlan";
        String viewType = ownerType + "View";
        String formModelType = ownerType + "FormModel";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        try {
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedName, formType);
            try (Writer writer = sourceFile.openWriter()) {
                writePackage(writer, packageName);
                writer.write("import cz.auderis.corusco.swing.behavior.BehaviorScope;\n");
                writer.write("import cz.auderis.corusco.swing.behavior.StandardBehaviors;\n");
                writer.write("import java.util.List;\n\n");
                writer.write("/**\n");
                writer.write(" * Generated behavior installation plan for {@link " + ownerType + "}.\n");
                writer.write(" */\n");
                writer.write("public final class " + generatedType + " {\n\n");
                writePrivateConstructor(writer, generatedType);
                writer.write("    /**\n");
                writer.write("     * Installs supported generated behaviors.\n");
                writer.write("     *\n");
                writer.write("     * @param view generated view contract\n");
                writer.write("     * @param model generated form model\n");
                writer.write("     * @param scope owning behavior scope\n");
                writer.write("     */\n");
                writer.write("    public static void install(" + viewType + " view, " + formModelType
                        + " model, BehaviorScope scope) {\n");
                writer.write("        java.util.Objects.requireNonNull(view, \"view\");\n");
                writer.write("        java.util.Objects.requireNonNull(model, \"model\");\n");
                writer.write("        java.util.Objects.requireNonNull(scope, \"scope\");\n");
                for (FieldSpec field : fields) {
                    writeBehaviorInstall(writer, field);
                }
                writer.write("    }\n");
                writer.write("}\n");
            }
        } catch (IOException e) {
            error(formType, "Could not write generated behavior plan: " + e.getMessage());
        }
    }

    private void writeBehaviorInstall(Writer writer, FieldSpec field) throws IOException {
        if ("TextFieldModel".equals(field.modelType)) {
            writer.write("        scope.install(view." + field.viewMethodName + "(), List.of(\n");
            writer.write("                StandardBehaviors.textFieldBinding(model." + field.componentName + "),\n");
            writer.write("                StandardBehaviors.validationTooltip(model." + field.componentName + ".problemSet()),\n");
            writer.write("                StandardBehaviors.validationBorder(model." + field.componentName + ".problemSet()),\n");
            writer.write("                StandardBehaviors.selectAllOnFocus()\n");
            writer.write("        ));\n");
        } else if ("CHECK_BOX".equals(field.kind)) {
            writer.write("        scope.install(view." + field.viewMethodName + "(), List.of(\n");
            writer.write("                StandardBehaviors.checkBoxBinding(model." + field.componentName + ")\n");
            writer.write("        ));\n");
        }
    }

    private void writeFieldModelInitialization(Writer writer, FieldSpec field, String fieldsType) throws IOException {
        if ("TextFieldModel".equals(field.modelType)) {
            writer.write("        this." + field.componentName + " = register(new TextFieldModel<>(\n");
            writer.write("                " + fieldsType + "." + field.constantName + ",\n");
            writer.write("                original." + field.componentName + "(),\n");
            writer.write("                " + field.converterExpression + "\n");
            writer.write("        ));\n");
        } else {
            writer.write("        this." + field.componentName + " = register(new FieldModel<>(\n");
            writer.write("                " + fieldsType + "." + field.constantName + ",\n");
            writer.write("                original." + field.componentName + "()\n");
            writer.write("        ));\n");
        }
    }

    private void writeDescriptorList(Writer writer, List<FieldSpec> fields, String descriptorsType) throws IOException {
        writer.write("    /**\n");
        writer.write("     * Returns generated field descriptors in record component order.\n");
        writer.write("     *\n");
        writer.write("     * @return descriptor list\n");
        writer.write("     */\n");
        writer.write("    public List<FieldDescriptor<?, ?>> descriptors() {\n");
        writer.write("        return List.of(\n");
        for (int i = 0; i < fields.size(); i++) {
            String suffix = i == fields.size() - 1 ? "\n" : ",\n";
            writer.write("                " + descriptorsType + "." + fields.get(i).constantName + suffix);
        }
        writer.write("        );\n");
        writer.write("    }\n\n");
    }

    private void writeBuildRules(
            Writer writer,
            List<FieldSpec> fields,
            String generatedType,
            String fieldsType
    ) throws IOException {
        writer.write("    private static RuleSet<" + generatedType + "> buildRules() {\n");
        writer.write("        RuleSet.Builder<" + generatedType + "> rules = RuleSet.builder();\n");
        for (FieldSpec field : fields) {
            for (ConstraintSpec constraint : field.constraints) {
                writeRule(writer, field, constraint, fieldsType);
            }
        }
        writer.write("        return rules.build();\n");
        writer.write("    }\n\n");
    }

    private void writeRule(
            Writer writer,
            FieldSpec field,
            ConstraintSpec constraint,
            String fieldsType
    ) throws IOException {
        String keyExpression = "TextFieldModel".equals(field.modelType)
                ? fieldsType + "." + field.constantName + ".asFieldKey()"
                : fieldsType + "." + field.constantName;
        String accessor = "model -> model." + field.componentName;
        String validator = validatorExpression(constraint);
        if ("TextFieldModel".equals(field.modelType)) {
            writer.write("        rules.field(" + keyExpression + ", " + accessor + ", " + validator + ");\n");
        } else {
            writer.write("        rules.semanticField(" + keyExpression + ", " + accessor + ", " + validator + ");\n");
        }
    }

    private String validatorExpression(ConstraintSpec constraint) {
        String message = stringLiteralOrNull(constraint.problemId);
        return switch (constraint.kind) {
            case "REQUIRED" -> "Validators.required(" + message + ")";
            case "LENGTH" -> "Validators.length(" + constraint.min + ", " + constraint.max + ", " + message + ")";
            case "DECIMAL_RANGE" -> "Validators.decimalRange("
                    + bigDecimalOrNull(constraint.min) + ", " + bigDecimalOrNull(constraint.max) + ", " + message + ")";
            case "INT_RANGE" -> "Validators.integerRange("
                    + constraint.min + ", " + constraint.max + ", " + message + ")";
            case "REGEX" -> "Validators.regex(Pattern.compile("
                    + stringLiteralOrNull(constraint.min) + "), " + message + ")";
            default -> throw new IllegalStateException("Unknown constraint kind: " + constraint.kind);
        };
    }

    private String resultValueExpression(FieldSpec field) {
        if ("TextFieldModel".equals(field.modelType)) {
            return field.componentName + ".value()";
        }
        return field.componentName + ".value().value()";
    }

    private void writeAction(Writer writer, ActionSpec action) throws IOException {
        writer.write("    /**\n");
        writer.write("     * Action key for {@code " + action.id + "}.\n");
        writer.write("     */\n");
        writer.write("    public static final ActionKey " + action.constantName + "_KEY =\n");
        writer.write("            ActionKey.of(" + stringLiteralOrNull(action.id) + ");\n\n");

        writer.write("    /**\n");
        writer.write("     * Text resource key for {@code " + action.textId + "}.\n");
        writer.write("     */\n");
        writer.write("    public static final ResourceKey<String> " + action.constantName + "_TEXT =\n");
        writer.write("            ResourceKey.of(" + stringLiteralOrNull(action.textId) + ", String.class);\n\n");

        if (action.tooltipId != null) {
            writer.write("    /**\n");
            writer.write("     * Tooltip resource key for {@code " + action.tooltipId + "}.\n");
            writer.write("     */\n");
            writer.write("    public static final ResourceKey<String> " + action.constantName + "_TOOLTIP =\n");
            writer.write("            ResourceKey.of(" + stringLiteralOrNull(action.tooltipId) + ", String.class);\n\n");
        }

        writer.write("    /**\n");
        writer.write("     * Action descriptor for {@code " + action.id + "}.\n");
        writer.write("     */\n");
        writer.write("    public static final ActionDescriptor " + action.constantName + " =\n");
        writer.write("            " + actionDescriptorExpression(action) + ";\n\n");
    }

    private String actionDescriptorExpression(ActionSpec action) {
        String expression = action.selectable
                ? "ActionDescriptor.toggle(" + action.constantName + "_KEY, " + action.constantName + "_TEXT)"
                : "ActionDescriptor.action(" + action.constantName + "_KEY, " + action.constantName + "_TEXT)";
        if (action.tooltipId != null) {
            expression += ".withTooltip(" + action.constantName + "_TOOLTIP)";
        }
        if (action.mnemonic != 0) {
            expression += ".withMnemonic(" + action.mnemonic + ")";
        }
        if (action.acceleratorKey != 0) {
            expression += ".withAccelerator(AcceleratorDescriptor.of("
                    + action.acceleratorKey + ", " + action.acceleratorModifiers + "))";
        }
        return expression;
    }

    private void writeResourceKey(Writer writer, String constantName, String id) throws IOException {
        writer.write("    /**\n");
        writer.write("     * Resource key for {@code " + id + "}.\n");
        writer.write("     */\n");
        writer.write("    public static final ResourceKey<String> " + constantName + " =\n");
        writer.write("            ResourceKey.of(" + stringLiteralOrNull(id) + ", String.class);\n\n");
    }

    private void writeDescriptor(
            Writer writer,
            FieldSpec field,
            String resourcesType,
            String problemsType
    ) throws IOException {
        writer.write("    /**\n");
        writer.write("     * Descriptor for {@code " + field.keyId + "}.\n");
        writer.write("     */\n");
        writer.write("    public static final FieldDescriptor<" + field.ownerType + ", " + field.valueType + "> "
                + field.constantName + " =\n");
        writer.write("            new FieldDescriptor<>(\n");
        writer.write("                    " + stringLiteralOrNull(field.keyId) + ",\n");
        writer.write("                    " + stringLiteralOrNull(field.componentName) + ",\n");
        writer.write("                    FieldKind." + field.kind + ",\n");
        writer.write("                    " + field.valueClass + ",\n");
        writer.write("                    " + resourcesType + "." + field.labelConstant + ",\n");
        writer.write("                    " + (field.tooltipConstant == null ? "null" : resourcesType + "." + field.tooltipConstant)
                + ",\n");
        writer.write("                    " + helpTopicExpression(field) + ",\n");
        writer.write("                    " + constraintsExpression(field, problemsType) + "\n");
        writer.write("            );\n\n");
    }

    private String helpTopicExpression(FieldSpec field) {
        return field.helpTopicId == null ? "null" : "HelpTopic.of(" + stringLiteralOrNull(field.helpTopicId) + ")";
    }

    private String constraintsExpression(FieldSpec field, String problemsType) {
        if (field.constraints.isEmpty()) {
            return "List.of()";
        }
        List<String> expressions = new ArrayList<>();
        for (ConstraintSpec constraint : field.constraints) {
            String problemReference = problemsType + "." + constraint.problemConstant;
            expressions.add(switch (constraint.kind) {
                case "REQUIRED" -> "ConstraintDescriptor.required(" + problemReference + ")";
                case "LENGTH" -> "ConstraintDescriptor.length(" + problemReference + ", "
                        + constraint.min + ", " + constraint.max + ")";
                case "DECIMAL_RANGE" -> "ConstraintDescriptor.decimalRange(" + problemReference + ", "
                        + stringLiteralOrNull(constraint.min) + ", " + stringLiteralOrNull(constraint.max) + ")";
                case "INT_RANGE" -> "ConstraintDescriptor.intRange(" + problemReference + ", "
                        + constraint.min + ", " + constraint.max + ")";
                case "REGEX" -> "ConstraintDescriptor.regex(" + problemReference + ", "
                        + stringLiteralOrNull(constraint.min) + ")";
                default -> throw new IllegalStateException("Unknown constraint kind: " + constraint.kind);
            });
        }
        return "List.of(" + String.join(", ", expressions) + ")";
    }

    private static void writePackage(Writer writer, String packageName) throws IOException {
        if (!packageName.isEmpty()) {
            writer.write("package " + packageName + ";\n\n");
        }
    }

    private static void writePrivateConstructor(Writer writer, String generatedType) throws IOException {
        writer.write("    private " + generatedType + "() {\n");
        writer.write("        throw new AssertionError(\"No instances\");\n");
        writer.write("    }\n\n");
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

    private static String emptyToNull(String value) {
        return value.isBlank() ? null : value;
    }

    private static String stringLiteralOrNull(String value) {
        return value == null ? "null" : "\"" + escape(value) + "\"";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String bigDecimalOrNull(String value) {
        return value == null ? "null" : "new BigDecimal(" + stringLiteralOrNull(value) + ")";
    }

    private record FieldSpec(
            String constantName,
            String keyId,
            String componentName,
            String ownerType,
            String valueType,
            String valueClass,
            String kind,
            String labelConstant,
            String tooltipConstant,
            String tooltipId,
            String helpTopicId,
            boolean textField,
            String modelType,
            String converterExpression,
            String viewComponentType,
            String viewMethodName,
            List<ConstraintSpec> constraints
    ) {
    }

    private record ConstraintSpec(
            String kind,
            String problemConstant,
            String problemId,
            String min,
            String max
    ) {
    }

    private record ActionSpec(
            String constantName,
            String id,
            String textId,
            String tooltipId,
            int mnemonic,
            int acceleratorKey,
            int acceleratorModifiers,
            boolean selectable
    ) {
    }
}
