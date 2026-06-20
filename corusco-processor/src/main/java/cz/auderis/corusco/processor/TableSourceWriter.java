package cz.auderis.corusco.processor;

import cz.auderis.corusco.processor.source.BasicStructuredFragment;
import cz.auderis.corusco.processor.source.SimpleMutableFragment;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Writes generated sources for {@code @CoruscoTable} row records.
 *
 * <p>The writer turns a validated {@link TableSpec} into resource-key,
 * column-metadata, descriptor, and Swing-binding source classes. Generated code
 * is deterministic and uses stable table/column ids rather than reflection or
 * localized header text. The writer reports failures to the annotation
 * processing environment and remains package-private because applications
 * should consume the generated classes, not the writer.</p>
 */
final class TableSourceWriter {

    private final Elements elements;
    private final Filer filer;
    private final Messager messager;

    TableSourceWriter(Elements elements, Filer filer, Messager messager) {
        this.elements = elements;
        this.filer = filer;
        this.messager = messager;
    }

    void writeTableSources(TypeElement tableType, TableSpec table) {
        writeTableResourcesClass(tableType, table);
        writeColumnsClass(tableType, table);
        writeTableDescriptorClass(tableType, table);
    }

    void writeTableSwingSources(TypeElement tableType, TableSpec table, String targetPackageName) {
        String sourcePackageName = elements.getPackageOf(tableType).getQualifiedName().toString();
        writeTableBindingsClass(tableType, table, targetPackageName, sourcePackageName);
    }

    private void writeTableResourcesClass(TypeElement tableType, TableSpec table) {
        String packageName = elements.getPackageOf(tableType).getQualifiedName().toString();
        String generatedType = table.ownerType + "TableResources";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = tableClassSource("table/resources-class.javafragment", packageName,
                table.ownerType, generatedType);
        source.addFragment(ProcessorSourceTemplates.privateConstructorSource(TableSourceWriter.class, generatedType));
        for (TableColumnSpec column : table.columns) {
            source.addFragment(resourceKeySource(column.headerConstant, column.headerId));
            if (column.tooltipConstant != null) {
                source.addFragment(resourceKeySource(column.tooltipConstant, column.tooltipId));
            }
        }
        writeSource(tableType, qualifiedName, source.asString(), "Could not write generated table resource keys");
    }

    private void writeColumnsClass(TypeElement tableType, TableSpec table) {
        String packageName = elements.getPackageOf(tableType).getQualifiedName().toString();
        String generatedType = table.ownerType + "Columns";
        String resourcesType = table.ownerType + "TableResources";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = tableClassSource("table/columns-class.javafragment", packageName,
                table.ownerType, generatedType);
        source.addFragment(ProcessorSourceTemplates.privateConstructorSource(TableSourceWriter.class, generatedType));
        source.addFragment(ProcessorSourceTemplates.fragment(TableSourceWriter.class, "table/table-key.javafragment", Map.of(
                "TABLE_ID", table.id,
                "OWNER_TYPE", table.ownerType,
                "TABLE_ID_LITERAL", stringLiteralOrNull(table.id)
        )));
        for (TableColumnSpec column : table.columns) {
            source.addFragment(columnSource(table, resourcesType, column));
        }
        writeSource(tableType, qualifiedName, source.asString(), "Could not write generated table columns");
    }

    private void writeTableDescriptorClass(TypeElement tableType, TableSpec table) {
        String packageName = elements.getPackageOf(tableType).getQualifiedName().toString();
        String generatedType = table.ownerType + "TableDescriptor";
        String columnsType = table.ownerType + "Columns";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = tableClassSource("table/descriptor-class.javafragment", packageName,
                table.ownerType, generatedType);
        source.addFragment(ProcessorSourceTemplates.privateConstructorSource(TableSourceWriter.class, generatedType));
        SimpleMutableFragment columnEntries = new SimpleMutableFragment();
        for (int i = 0; i < table.columns.size(); i++) {
            String suffix = i == table.columns.size() - 1 ? "\n" : ",\n";
            columnEntries.append("                        ").append(columnsType).append(".")
                    .append(table.columns.get(i).constantName).append(suffix);
        }
        source.addFragment(ProcessorSourceTemplates.fragment(TableSourceWriter.class, "table/descriptor-members.javafragment", Map.of(
                "OWNER_TYPE", table.ownerType,
                "COLUMNS_TYPE", columnsType,
                "COLUMN_ENTRIES", columnEntries.asString()
        )));
        writeSource(tableType, qualifiedName, source.asString(), "Could not write generated table descriptor");
    }

    private void writeTableBindingsClass(
            TypeElement tableType,
            TableSpec table,
            String packageName,
            String sourcePackageName
    ) {
        String generatedType = table.ownerType + "TableBindings";
        String descriptorType = generatedPeerType(sourcePackageName, packageName, table.ownerType + "TableDescriptor");
        String ownerType = generatedPeerType(sourcePackageName, packageName, table.ownerType);
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = tableClassSource("table/bindings-class.javafragment", packageName,
                table.ownerType, generatedType);
        source.addFragment(ProcessorSourceTemplates.privateConstructorSource(TableSourceWriter.class, generatedType));
        source.addFragment(ProcessorSourceTemplates.fragment(TableSourceWriter.class, "table/binding-members.javafragment", Map.of(
                "OWNER_TYPE", ownerType,
                "DESCRIPTOR_TYPE", descriptorType
        )));
        writeSource(tableType, qualifiedName, source.asString(), "Could not write generated table bindings");
    }

    private String columnSource(TableSpec table, String resourcesType, TableColumnSpec column) {
        String tooltip = column.tooltipConstant == null ? "null" : resourcesType + "." + column.tooltipConstant;
        String helpTopic = column.helpTopicId == null ? "null" : "HelpTopic.of(" + stringLiteralOrNull(column.helpTopicId) + ")";
        BasicStructuredFragment source = new BasicStructuredFragment();
        try {
            source.loadResource(TableSourceWriter.class, "templates/table/column.javafragment");
        } catch (IOException e) {
            throw new IllegalStateException("Could not read generated source template table/column.javafragment", e);
        }
        source.replaceLocal(Map.ofEntries(
                entry("COLUMN_KEY_ID", column.keyId),
                entry("OWNER_TYPE", column.ownerType),
                entry("VALUE_TYPE", column.valueType),
                entry("CONSTANT_NAME", column.constantName),
                entry("COLUMN_KEY_ID_LITERAL", stringLiteralOrNull(column.keyId)),
                entry("VALUE_CLASS", column.valueClass),
                entry("RESOURCES_TYPE", resourcesType),
                entry("HEADER_CONSTANT", column.headerConstant),
                entry("TOOLTIP_EXPRESSION", tooltip),
                entry("HELP_TOPIC_EXPRESSION", helpTopic),
                entry("PERSISTENCE_ID_LITERAL", stringLiteralOrNull(column.persistenceId)),
                entry("MIN_WIDTH", Integer.toString(column.minWidth)),
                entry("MAX_WIDTH", intLiteral(column.maxWidth)),
                entry("WIDTH", Integer.toString(column.width)),
                entry("ORDER", Integer.toString(column.order)),
                entry("VISIBLE", Boolean.toString(column.visible)),
                entry("SORTABLE", Boolean.toString(column.sortable)),
                entry("FILTERABLE", Boolean.toString(column.filterable)),
                entry("EDITABLE", Boolean.toString(column.editable)),
                entry("HIDEABLE", Boolean.toString(column.hideable))
        ));
        source.addFragment(column.editable ? editableColumnSource(column) : readOnlyColumnSource(column));
        source.addFragment(updaterSource(table, column));
        return source.asString();
    }

    private String readOnlyColumnSource(TableColumnSpec column) {
        return ProcessorSourceTemplates.fragment(TableSourceWriter.class, "table/read-only-column.javafragment", Map.of(
                "COLUMN_KEY_ID", column.keyId,
                "OWNER_TYPE", column.ownerType,
                "VALUE_TYPE", column.valueType,
                "CONSTANT_NAME", column.constantName,
                "COMPONENT_NAME", column.componentName
        )).asString();
    }

    private String editableColumnSource(TableColumnSpec column) {
        return ProcessorSourceTemplates.fragment(TableSourceWriter.class, "table/editable-column.javafragment", Map.of(
                "COLUMN_KEY_ID", column.keyId,
                "OWNER_TYPE", column.ownerType,
                "VALUE_TYPE", column.valueType,
                "CONSTANT_NAME", column.constantName,
                "COMPONENT_NAME", column.componentName,
                "COLUMNS_TYPE", column.ownerType + "Columns",
                "UPDATER_NAME", updaterName(column)
        )).asString();
    }

    private String updaterSource(TableSpec table, TableColumnSpec column) {
        if (!column.editable) {
            return "";
        }
        SimpleMutableFragment arguments = new SimpleMutableFragment();
        for (int i = 0; i < table.components.size(); i++) {
            TableComponentSpec component = table.components.get(i);
            String suffix = i == table.components.size() - 1 ? "\n" : ",\n";
            String expression = component.name.equals(column.componentName)
                    ? "value"
                    : "row." + component.name + "()";
            arguments.appendFormatted("                %s%s", expression, suffix);
        }
        return ProcessorSourceTemplates.fragment(TableSourceWriter.class, "table/updater.javafragment", Map.of(
                "OWNER_TYPE", column.ownerType,
                "UPDATER_NAME", updaterName(column),
                "VALUE_TYPE", column.valueType,
                "UPDATER_ARGUMENTS", arguments.asString()
        )).asString();
    }

    private static String resourceKeySource(String constantName, String resourceId) {
        return ProcessorSourceTemplates.fragment(TableSourceWriter.class, "common/resource-key.javafragment", Map.of(
                "RESOURCE_ID", resourceId,
                "CONSTANT_NAME", constantName,
                "RESOURCE_ID_LITERAL", stringLiteralOrNull(resourceId)
        )).asString();
    }

    private static BasicStructuredFragment tableClassSource(
            String resourceName,
            String packageName,
            String ownerType,
            String generatedType
    ) {
        return ProcessorSourceTemplates.structuredClass(TableSourceWriter.class, resourceName, packageName, Map.of(
                "OWNER_TYPE", ownerType,
                "GENERATED_TYPE", generatedType
        ));
    }

    private static String generatedPeerType(String sourcePackageName, String targetPackageName, String typeName) {
        if (sourcePackageName.equals(targetPackageName) || sourcePackageName.isEmpty()) {
            return typeName;
        }
        return sourcePackageName + "." + typeName;
    }

    private static String updaterName(TableColumnSpec column) {
        StringBuilder result = new StringBuilder("update");
        for (String part : column.constantName.split("_")) {
            result.append(part.charAt(0)).append(part.substring(1).toLowerCase(java.util.Locale.ROOT));
        }
        return result.toString();
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

    private static String intLiteral(int value) {
        return value == Integer.MAX_VALUE ? "Integer.MAX_VALUE" : Integer.toString(value);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
