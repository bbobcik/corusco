package cz.auderis.corusco.processor;

import cz.auderis.corusco.processor.source.BasicStructuredFragment;
import cz.auderis.corusco.processor.source.SimpleMutableFragment;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Objects;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Writes generated sources for {@code @CoruscoDataSet} records.
 */
final class DataSetSourceWriter {

    private final Elements elements;
    private final Filer filer;
    private final Messager messager;

    DataSetSourceWriter(Elements elements, Filer filer, Messager messager) {
        this.elements = elements;
        this.filer = filer;
        this.messager = messager;
    }

    void writeDataSetSources(TypeElement dataSetType, DataSetSpec dataSet) {
        writeColumnsClass(dataSetType, dataSet);
        writeDescriptorClass(dataSetType, dataSet);
        writeFrameClass(dataSetType, dataSet);
    }

    private void writeFrameClass(TypeElement dataSetType, DataSetSpec dataSet) {
        String packageName = elements.getPackageOf(dataSetType).getQualifiedName().toString();
        String generatedType = dataSet.ownerType + "Frame";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = dataSetClassSource("dataset/frame-class.javafragment", packageName,
                dataSet.ownerType, generatedType);
        source.replaceLocal(frameValues(dataSet));
        writeSource(dataSetType, qualifiedName, source.asString(), "Could not write generated data-set frame");
    }

    private void writeColumnsClass(TypeElement dataSetType, DataSetSpec dataSet) {
        String packageName = elements.getPackageOf(dataSetType).getQualifiedName().toString();
        String generatedType = dataSet.ownerType + "DataColumns";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = dataSetClassSource("dataset/columns-class.javafragment", packageName,
                dataSet.ownerType, generatedType);
        source.addFragment(ProcessorSourceTemplates.privateConstructorSource(DataSetSourceWriter.class, generatedType));
        source.addFragment(ProcessorSourceTemplates.fragment(DataSetSourceWriter.class, "dataset/data-set-key.javafragment", Map.of(
                "OWNER_TYPE", dataSet.ownerType,
                "DATA_SET_ID_LITERAL", stringLiteral(dataSet.id)
        )));
        for (DataSetColumnSpec column : dataSet.columns) {
            source.addFragment(columnSource(column));
        }
        writeSource(dataSetType, qualifiedName, source.asString(), "Could not write generated data-set columns");
    }

    private void writeDescriptorClass(TypeElement dataSetType, DataSetSpec dataSet) {
        String packageName = elements.getPackageOf(dataSetType).getQualifiedName().toString();
        String generatedType = dataSet.ownerType + "DataDescriptor";
        String columnsType = dataSet.ownerType + "DataColumns";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = dataSetClassSource("dataset/descriptor-class.javafragment", packageName,
                dataSet.ownerType, generatedType);
        source.addFragment(ProcessorSourceTemplates.privateConstructorSource(DataSetSourceWriter.class, generatedType));
        source.addFragment(ProcessorSourceTemplates.fragment(DataSetSourceWriter.class,
                "dataset/descriptor-members.javafragment", Map.of(
                        "OWNER_TYPE", dataSet.ownerType,
                        "COLUMNS_TYPE", columnsType,
                        "COLUMN_ENTRIES", descriptorEntries(dataSet, columnsType)
                )));
        writeSource(dataSetType, qualifiedName, source.asString(), "Could not write generated data-set descriptor");
    }

    private static String columnSource(DataSetColumnSpec column) {
        return ProcessorSourceTemplates.fragment(DataSetSourceWriter.class, "dataset/column.javafragment", Map.ofEntries(
                Map.entry("OWNER_TYPE", column.ownerType),
                Map.entry("VALUE_TYPE", column.valueType),
                Map.entry("CONSTANT_NAME", column.constantName),
                Map.entry("COLUMN_ID_LITERAL", stringLiteral(column.keyId)),
                Map.entry("VALUE_CLASS", column.valueClass),
                Map.entry("COMPONENT_NAME_LITERAL", stringLiteral(column.componentName)),
                Map.entry("ROLE", column.role),
                Map.entry("STORAGE", column.storage),
                Map.entry("UNIT_EXPRESSION", unitExpression(column.unit)),
                Map.entry("MISSING_POLICY", column.missingPolicy),
                Map.entry("QUALITY_POLICY", column.qualityPolicy),
                Map.entry("AGGREGATIONS_EXPRESSION", column.aggregationsExpression)
        )).asString();
    }

    private static Map<String, String> frameValues(DataSetSpec dataSet) {
        SimpleMutableFragment fields = new SimpleMutableFragment();
        SimpleMutableFragment copyAssignments = new SimpleMutableFragment();
        SimpleMutableFragment accessors = new SimpleMutableFragment();
        SimpleMutableFragment rowArguments = new SimpleMutableFragment();
        SimpleMutableFragment builderFields = new SimpleMutableFragment();
        SimpleMutableFragment builderInitializers = new SimpleMutableFragment();
        SimpleMutableFragment builderAddAssignments = new SimpleMutableFragment();
        SimpleMutableFragment builderGrowAssignments = new SimpleMutableFragment();
        for (int i = 0; i < dataSet.columns.size(); i++) {
            DataSetColumnSpec column = dataSet.columns.get(i);
            fields.append(frameColumnFragment("frame-field.javafragment", column, Map.of(
                    "ARRAY_TYPE", arrayType(column)
            )));
            copyAssignments.append(frameColumnFragment("frame-copy-assignment.javafragment", column, Map.of()));
            accessors.append(frameColumnFragment("frame-accessor.javafragment", column, Map.of(
                    "SOURCE_TYPE", column.sourceType,
                    "READ_EXPRESSION", readExpression(column, "this.", "row")
            )));
            String suffix = i == dataSet.columns.size() - 1 ? "\n" : ",\n";
            rowArguments.append("                ").append(readExpression(column, "this.", "row")).append(suffix);
            builderFields.append(frameColumnFragment("frame-builder-field.javafragment", column, Map.of(
                    "ARRAY_TYPE", arrayType(column)
            )));
            builderInitializers.append(frameColumnFragment("frame-builder-initializer.javafragment", column, Map.of(
                    "ARRAY_COMPONENT_TYPE", arrayComponentType(column)
            )));
            builderAddAssignments.append(frameColumnFragment("frame-builder-add-assignment.javafragment", column, Map.of()));
            builderGrowAssignments.append(frameColumnFragment("frame-builder-grow-assignment.javafragment", column, Map.of()));
        }
        return Map.ofEntries(
                Map.entry("OWNER_TYPE", dataSet.ownerType),
                Map.entry("GENERATED_TYPE", dataSet.ownerType + "Frame"),
                Map.entry("FRAME_FIELDS", fields.asString()),
                Map.entry("FRAME_COPY_ASSIGNMENTS", copyAssignments.asString()),
                Map.entry("FRAME_ACCESSORS", accessors.asString()),
                Map.entry("ROW_ARGUMENTS", rowArguments.asString()),
                Map.entry("BUILDER_FIELDS", builderFields.asString()),
                Map.entry("BUILDER_INITIALIZERS", builderInitializers.asString()),
                Map.entry("BUILDER_ADD_ASSIGNMENTS", builderAddAssignments.asString()),
                Map.entry("BUILDER_GROW_ASSIGNMENTS", builderGrowAssignments.asString()),
                Map.entry("FIRST_COLUMN", dataSet.columns.get(0).componentName)
        );
    }

    private static String frameColumnFragment(
            String resourceName,
            DataSetColumnSpec column,
            Map<String, String> values
    ) {
        SimpleMutableFragment fragment = ProcessorSourceTemplates.fragment(DataSetSourceWriter.class,
                "dataset/" + resourceName,
                values);
        fragment.replaceLocal("COMPONENT_NAME", column.componentName);
        return fragment.asString();
    }

    private static String descriptorEntries(DataSetSpec dataSet, String columnsType) {
        SimpleMutableFragment entries = new SimpleMutableFragment();
        for (int i = 0; i < dataSet.columns.size(); i++) {
            String suffix = i == dataSet.columns.size() - 1 ? "\n" : ",\n";
            entries.append("                            ").append(columnsType).append(".")
                    .append(dataSet.columns.get(i).constantName).append(suffix);
        }
        return entries.asString();
    }

    private static BasicStructuredFragment dataSetClassSource(
            String resourceName,
            String packageName,
            String ownerType,
            String generatedType
    ) {
        return ProcessorSourceTemplates.structuredClass(DataSetSourceWriter.class, resourceName, packageName, Map.of(
                "OWNER_TYPE", ownerType,
                "GENERATED_TYPE", generatedType
        ));
    }

    private static String unitExpression(String unit) {
        return unit == null || unit.isBlank() ? "null" : "UnitMetadata.of(" + stringLiteral(unit) + ")";
    }

    private static String arrayType(DataSetColumnSpec column) {
        return arrayComponentType(column) + "[]";
    }

    private static String arrayComponentType(DataSetColumnSpec column) {
        return switch (column.storage) {
            case "BOOLEAN_ARRAY" -> "boolean";
            case "BYTE_ARRAY" -> "byte";
            case "SHORT_ARRAY" -> "short";
            case "INT_ARRAY" -> "int";
            case "CHAR_ARRAY" -> "char";
            case "LONG_ARRAY" -> "long";
            case "FLOAT_ARRAY" -> "float";
            case "DOUBLE_ARRAY" -> "double";
            default -> "java.lang.Object";
        };
    }

    private static String readExpression(DataSetColumnSpec column, String ownerPrefix, String rowExpression) {
        String expression = ownerPrefix + column.componentName + "[" + rowExpression + "]";
        if ("OBJECT_ARRAY".equals(column.storage)) {
            return "(" + column.sourceType + ") " + expression;
        }
        return expression;
    }

    private void writeSource(TypeElement originatingType, String qualifiedName, String source, String errorPrefix) {
        Objects.requireNonNull(source, "source");
        try {
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedName, originatingType);
            try (Writer writer = sourceFile.openWriter()) {
                writer.write(source);
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, errorPrefix + ": " + e.getMessage(), originatingType);
        }
    }

    private static String stringLiteral(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
