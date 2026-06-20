package cz.auderis.corusco.processor;

import cz.auderis.corusco.processor.source.BasicStructuredFragment;
import cz.auderis.corusco.processor.source.SimpleMutableFragment;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Writes generated enum option metadata for form fields.
 */
final class FormOptionsSourceWriter {

    private final Elements elements;
    private final Filer filer;
    private final Messager messager;

    FormOptionsSourceWriter(Elements elements, Filer filer, Messager messager) {
        this.elements = elements;
        this.filer = filer;
        this.messager = messager;
    }

    void writeOptionsClass(TypeElement formType, FormSpec form) {
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Options";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = ProcessorSourceTemplates.structuredClass(
                FormOptionsSourceWriter.class,
                "options/class.javafragment",
                packageName,
                Map.of(
                        "OWNER_TYPE", ownerType,
                        "GENERATED_TYPE", generatedType
                )
        );
        source.addFragment(ProcessorSourceTemplates.privateConstructorSource(FormOptionsSourceWriter.class, generatedType));
        for (FieldSpec field : form.fields) {
            if (!field.options.isEmpty()) {
                source.addFragment(enumOptionSource(field));
            }
        }
        writeSource(formType, qualifiedName, source.asString(), "Could not write generated option metadata");
    }

    private String enumOptionSource(FieldSpec field) {
        SimpleMutableFragment source = new SimpleMutableFragment();
        source.append(ProcessorSourceTemplates.fragment(FormOptionsSourceWriter.class, "options/field-prefix.javafragment", Map.of(
                "FIELD_CONSTANT", field.constantName,
                "OWNER_TYPE", field.ownerType
        )));
        for (OptionSpec option : field.options) {
            String keyConstant = field.constantName + "_" + option.constantName + "_KEY";
            String descriptorConstant = field.constantName + "_" + option.constantName;
            source.append(ProcessorSourceTemplates.fragment(FormOptionsSourceWriter.class, "options/option.javafragment", Map.of(
                    "OPTION_KEY", option.key,
                    "KEY_CONSTANT", keyConstant,
                    "OPTION_KEY_LITERAL", stringLiteralOrNull(option.key),
                    "OPTION_DESCRIPTOR_ID", field.keyId + "/" + option.key,
                    "VALUE_TYPE", field.valueType,
                    "DESCRIPTOR_CONSTANT", descriptorConstant,
                    "ENUM_CONSTANT", option.enumConstantName,
                    "FIELD_CONSTANT", field.constantName
            )));
        }
        List<String> optionDescriptors = new ArrayList<>();
        List<String> optionValues = new ArrayList<>();
        for (OptionSpec option : field.options) {
            optionDescriptors.add(field.constantName + "_" + option.constantName);
            optionValues.add(field.valueType + "." + option.enumConstantName);
        }
        source.append(ProcessorSourceTemplates.fragment(FormOptionsSourceWriter.class, "options/field-lists.javafragment", Map.of(
                "FIELD_KEY_ID", field.keyId,
                "VALUE_TYPE", field.valueType,
                "FIELD_CONSTANT", field.constantName,
                "OPTION_DESCRIPTORS", String.join(", ", optionDescriptors),
                "OPTION_VALUES", String.join(", ", optionValues)
        )));
        return source.asString();
    }

    private void writeSource(TypeElement originatingType, String qualifiedName, String source, String errorPrefix) {
        try {
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedName, originatingType);
            try (Writer writer = sourceFile.openWriter()) {
                writer.write(source);
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, errorPrefix + ": " + e.getMessage(), originatingType);
        }
    }

    private static String stringLiteralOrNull(String value) {
        return value == null ? "null" : "\"" + escape(value) + "\"";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
