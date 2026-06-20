package cz.auderis.corusco.processor;

import cz.auderis.corusco.processor.source.BasicStructuredFragment;
import cz.auderis.corusco.processor.source.SimpleMutableFragment;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Writes generated dependency metadata for component-state models.
 */
final class FormDependenciesSourceWriter {

    private final Elements elements;
    private final Filer filer;
    private final Messager messager;

    FormDependenciesSourceWriter(Elements elements, Filer filer, Messager messager) {
        this.elements = elements;
        this.filer = filer;
        this.messager = messager;
    }

    void writeDependenciesClass(TypeElement formType, FormSpec form) {
        List<DependencySpec> dependencies = form.componentStates.stream()
                .flatMap(state -> state.dependencies.stream())
                .toList();
        if (dependencies.isEmpty()) {
            return;
        }
        String packageName = elements.getPackageOf(formType).getQualifiedName().toString();
        String ownerType = form.sourceType;
        String generatedType = ownerType + "Dependencies";
        String fieldsType = ownerType + "Fields";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = ProcessorSourceTemplates.structuredClass(
                FormDependenciesSourceWriter.class,
                "dependencies/class.javafragment",
                packageName,
                Map.of(
                        "OWNER_TYPE", ownerType,
                        "GENERATED_TYPE", generatedType
                )
        );
        source.addFragment(ProcessorSourceTemplates.privateConstructorSource(FormDependenciesSourceWriter.class, generatedType));
        for (DependencySpec dependency : dependencies) {
            source.addFragment(dependencySource(dependency, fieldsType));
        }
        source.addFragment(allDependenciesSource(dependencies));
        writeSource(formType, qualifiedName, source.asString(), "Could not write generated field dependencies");
    }

    private String dependencySource(DependencySpec dependency, String fieldsType) {
        return ProcessorSourceTemplates.fragment(FormDependenciesSourceWriter.class, "dependencies/dependency.javafragment", Map.of(
                "SOURCE_FIELD_NAME", dependency.sourceFieldName,
                "TARGET_STATE_MODEL", dependency.targetStateModel,
                "CONSTANT_NAME", dependency.constantName,
                "FIELDS_TYPE", fieldsType,
                "SOURCE_FIELD_KEY", dependency.sourceTextField
                        ? dependency.sourceFieldConstant + ".asFieldKey()"
                        : dependency.sourceFieldConstant,
                "TARGET_STATE_MODEL_LITERAL", stringLiteralOrNull(dependency.targetStateModel),
                "DEPENDENCY_VALUES", dependencyValuesExpression(dependency.values),
                "DEPENDENCY_EFFECT", dependency.effect.toString()
        )).asString();
    }

    private static String allDependenciesSource(List<DependencySpec> dependencies) {
        SimpleMutableFragment entries = new SimpleMutableFragment();
        for (int i = 0; i < dependencies.size(); i++) {
            String suffix = i == dependencies.size() - 1 ? "\n" : ",\n";
            entries.append("                ").append(dependencies.get(i).constantName).append(suffix);
        }
        return ProcessorSourceTemplates.fragment(FormDependenciesSourceWriter.class, "dependencies/all.javafragment", Map.of(
                "DEPENDENCY_ENTRIES", entries.asString()
        )).asString();
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

    private static String dependencyValuesExpression(List<String> values) {
        return values.stream()
                .map(FormDependenciesSourceWriter::stringLiteralOrNull)
                .collect(Collectors.joining(", "));
    }

    private static String stringLiteralOrNull(String value) {
        return value == null ? "null" : "\"" + escape(value) + "\"";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
