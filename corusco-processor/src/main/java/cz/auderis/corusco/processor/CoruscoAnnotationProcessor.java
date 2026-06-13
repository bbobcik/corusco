package cz.auderis.corusco.processor;

import cz.auderis.corusco.annotations.CheckBox;
import cz.auderis.corusco.annotations.DecimalRange;
import cz.auderis.corusco.annotations.Help;
import cz.auderis.corusco.annotations.Length;
import cz.auderis.corusco.annotations.Required;
import cz.auderis.corusco.annotations.SwingForm;
import cz.auderis.corusco.annotations.TextField;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
@SupportedAnnotationTypes("cz.auderis.corusco.annotations.SwingForm")
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
        return true;
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
            if (!textField && !checkBox) {
                if (hasFieldMetadata(component)) {
                    error(component, "Field metadata annotations require @TextField or @CheckBox");
                    failed = true;
                }
                continue;
            }
            if (textField && checkBox) {
                error(component, "Record component cannot be both @TextField and @CheckBox");
                failed = true;
                continue;
            }
            if (checkBox && !isBoolean(component.asType())) {
                error(component, "@CheckBox requires boolean or java.lang.Boolean component type");
                failed = true;
                continue;
            }
            if (!isSupportedValueType(component.asType())) {
                error(component, "Generated field keys require primitive or declared component types");
                failed = true;
                continue;
            }
            FieldSpec field = fieldSpec(formType, annotation.id(), component, textField);
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
    }

    private FieldSpec fieldSpec(
            TypeElement formType,
            String formId,
            RecordComponentElement component,
            boolean textField
    ) {
        String componentName = component.getSimpleName().toString();
        String constantName = constantName(componentName);
        String keyId = formId + "/" + kebab(componentName);
        String ownerType = formType.getSimpleName().toString();
        String valueType = genericType(component.asType());
        String valueClass = classLiteral(component.asType());
        String kind = textField ? "TEXT" : "CHECK_BOX";
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
                textField,
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
        Length length = component.getAnnotation(Length.class);
        if (length != null && (length.min() < 0 || length.max() < length.min())) {
            error(component, "@Length requires 0 <= min <= max");
            valid = false;
        }
        DecimalRange decimalRange = component.getAnnotation(DecimalRange.class);
        if (decimalRange != null && !validateDecimalRange(component, decimalRange)) {
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
}
