package cz.auderis.corusco.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

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
        writeColumnsClass(tableType, table);
        writeTableDescriptorClass(tableType, table);
    }

    private void writeColumnsClass(TypeElement tableType, TableSpec table) {
        String packageName = elements.getPackageOf(tableType).getQualifiedName().toString();
        String generatedType = table.ownerType + "Columns";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.core.key.ResourceKey;
                import cz.auderis.corusco.core.table.Column;
                import cz.auderis.corusco.core.table.ColumnCapabilities;
                import cz.auderis.corusco.core.table.ColumnDefaults;
                import cz.auderis.corusco.core.table.ColumnDescriptor;
                import cz.auderis.corusco.core.table.ColumnKey;
                import cz.auderis.corusco.core.table.TableKey;

                /**
                 * Generated table column metadata for {@link %s}.
                 */
                public final class %s {

                """.formatted(table.ownerType, generatedType));
        source.append(privateConstructorSource(generatedType));
        source.append("""
                    /**
                     * Table key for {@code %s}.
                     */
                    public static final TableKey<%s> TABLE =
                            TableKey.of(%s, %s.class);

                """.formatted(table.id, table.ownerType, stringLiteralOrNull(table.id), table.ownerType));
        for (TableColumnSpec column : table.columns) {
            source.append(columnSource(table, column));
        }
        source.append("}\n");
        writeSource(tableType, qualifiedName, source.toString(), "Could not write generated table columns");
    }

    private void writeTableDescriptorClass(TypeElement tableType, TableSpec table) {
        String packageName = elements.getPackageOf(tableType).getQualifiedName().toString();
        String generatedType = table.ownerType + "TableDescriptor";
        String columnsType = table.ownerType + "Columns";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.core.collection.ObservableList;
                import cz.auderis.corusco.swing.table.ObservableTableModel;
                import java.util.List;

                /**
                 * Generated table descriptor for {@link %s}.
                 */
                public final class %s {

                """.formatted(table.ownerType, generatedType));
        source.append(privateConstructorSource(generatedType));
        source.append("""
                    /**
                     * Generated table descriptor.
                     */
                    public static final cz.auderis.corusco.core.table.TableDescriptor<%s> DESCRIPTOR =
                            new cz.auderis.corusco.core.table.TableDescriptor<>(
                                    %s.TABLE,
                                    List.of(
                """.formatted(table.ownerType, columnsType));
        for (int i = 0; i < table.columns.size(); i++) {
            String suffix = i == table.columns.size() - 1 ? "\n" : ",\n";
            source.append("                        ").append(columnsType).append(".")
                    .append(table.columns.get(i).constantName).append(suffix);
        }
        source.append("""
                                    )
                            );

                    /**
                     * Creates an observable Swing table model.
                     *
                     * @param rows observable row source
                     * @return table model
                     */
                    public static ObservableTableModel<%s> tableModel(ObservableList<%s> rows) {
                        return ObservableTableModel.of(rows, DESCRIPTOR);
                    }
                }
                """.formatted(table.ownerType, table.ownerType));
        writeSource(tableType, qualifiedName, source.toString(), "Could not write generated table descriptor");
    }

    private String columnSource(TableSpec table, TableColumnSpec column) {
        String tooltip = column.tooltipConstant == null ? "null" : column.tooltipConstant;
        String columnFactory = column.editable ? editableColumnSource(column) : readOnlyColumnSource(column);
        return """
                    /**
                     * Column key for {@code %s}.
                     */
                    public static final ColumnKey<%s, %s> %s_KEY =
                            ColumnKey.of(%s, %s.class, %s);

                    /**
                     * Header resource key for {@code %s}.
                     */
                    public static final ResourceKey<String> %s =
                            ResourceKey.of(%s, String.class);

                %s\
                    /**
                     * Column descriptor for {@code %s}.
                     */
                    public static final ColumnDescriptor<%s, %s> %s_DESCRIPTOR =
                            new ColumnDescriptor<>(
                                    %s_KEY,
                                    %s,
                                    %s,
                                    new ColumnDefaults(%d, %d, %s),
                                    new ColumnCapabilities(%s, %s, %s, %s)
                            );

                %s\
                """.formatted(
                column.keyId,
                column.ownerType,
                column.valueType,
                column.constantName,
                stringLiteralOrNull(column.keyId),
                column.ownerType,
                column.valueClass,
                column.headerId,
                column.headerConstant,
                stringLiteralOrNull(column.headerId),
                tooltipSource(column),
                column.keyId,
                column.ownerType,
                column.valueType,
                column.constantName,
                column.constantName,
                column.headerConstant,
                tooltip,
                column.width,
                column.order,
                Boolean.toString(column.visible),
                Boolean.toString(column.sortable),
                Boolean.toString(column.filterable),
                Boolean.toString(column.editable),
                Boolean.toString(column.hideable),
                columnFactory
        ) + updaterSource(table, column);
    }

    private String readOnlyColumnSource(TableColumnSpec column) {
        return """
                    /**
                     * Read-only column for {@code %s}.
                     */
                    public static final Column<%s, %s> %s =
                            Column.readOnly(%s_DESCRIPTOR, %s::%s);

                """.formatted(
                column.keyId,
                column.ownerType,
                column.valueType,
                column.constantName,
                column.constantName,
                column.ownerType,
                column.componentName
        );
    }

    private String editableColumnSource(TableColumnSpec column) {
        return """
                    /**
                     * Editable column for {@code %s}.
                     */
                    public static final Column<%s, %s> %s =
                            Column.editable(%s_DESCRIPTOR, %s::%s, %s::%s);

                """.formatted(
                column.keyId,
                column.ownerType,
                column.valueType,
                column.constantName,
                column.constantName,
                column.ownerType,
                column.componentName,
                column.ownerType + "Columns",
                updaterName(column)
        );
    }

    private String updaterSource(TableSpec table, TableColumnSpec column) {
        if (!column.editable) {
            return "";
        }
        StringBuilder arguments = new StringBuilder();
        for (int i = 0; i < table.components.size(); i++) {
            TableComponentSpec component = table.components.get(i);
            String suffix = i == table.components.size() - 1 ? "\n" : ",\n";
            String expression = component.name.equals(column.componentName)
                    ? "value"
                    : "row." + component.name + "()";
            arguments.append("                ").append(expression).append(suffix);
        }
        return """
                    private static %s %s(%s row, %s value) {
                        return new %s(
                %s\
                        );
                    }

                """.formatted(
                column.ownerType,
                updaterName(column),
                column.ownerType,
                column.valueType,
                column.ownerType,
                arguments
        );
    }

    private String tooltipSource(TableColumnSpec column) {
        if (column.tooltipConstant == null) {
            return "";
        }
        return """
                    /**
                     * Tooltip resource key for {@code %s}.
                     */
                    public static final ResourceKey<String> %s =
                            ResourceKey.of(%s, String.class);

                """.formatted(column.tooltipId, column.tooltipConstant, stringLiteralOrNull(column.tooltipId));
    }

    private static StringBuilder sourceBuilder(String packageName) {
        StringBuilder source = new StringBuilder();
        if (!packageName.isEmpty()) {
            source.append("package ").append(packageName).append(";\n\n");
        }
        return source;
    }

    private static String privateConstructorSource(String generatedType) {
        return """
                    private %s() {
                        throw new AssertionError("No instances");
                    }

                """.formatted(generatedType);
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

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
