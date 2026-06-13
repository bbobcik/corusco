package cz.auderis.corusco.processor;

import cz.auderis.corusco.annotations.CheckBox;
import cz.auderis.corusco.annotations.SwingForm;
import cz.auderis.corusco.annotations.TextField;
import java.io.IOException;
import java.io.Writer;
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
            fields.add(fieldSpec(formType, annotation.id(), component, textField));
        }

        if (fields.isEmpty() && !failed) {
            error(formType, "@SwingForm record must contain at least one @TextField or @CheckBox component");
            return;
        }
        if (failed) {
            return;
        }
        writeFieldsClass(formType, fields);
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
        return new FieldSpec(constantName, keyId, ownerType, valueType, valueClass, textField);
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
        writer.write("            " + factory + ".of(\"" + field.keyId + "\", "
                + field.ownerType + ".class, " + field.valueClass + ");\n\n");
    }

    private boolean isBoolean(TypeMirror type) {
        if (type.getKind() == TypeKind.BOOLEAN) {
            return true;
        }
        return "java.lang.Boolean".equals(types.erasure(type).toString());
    }

    private static boolean isSupportedValueType(TypeMirror type) {
        return type.getKind().isPrimitive() || type.getKind() == TypeKind.DECLARED;
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

    private record FieldSpec(
            String constantName,
            String keyId,
            String ownerType,
            String valueType,
            String valueClass,
            boolean textField
    ) {
    }
}
