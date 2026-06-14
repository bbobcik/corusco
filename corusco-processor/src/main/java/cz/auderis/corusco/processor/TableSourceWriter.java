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

/**
 * Writes generated sources for {@code @SwingTable} row records.
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
        writeTableBindingsClass(tableType, table);
    }

    private void writeTableResourcesClass(TypeElement tableType, TableSpec table) {
        String packageName = elements.getPackageOf(tableType).getQualifiedName().toString();
        String generatedType = table.ownerType + "TableResources";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.core.key.ResourceKey;

                /**
                 * Generated table resource keys for {@link %s}.
                 */
                public final class %s {

                """.formatted(table.ownerType, generatedType));
        source.append(privateConstructorSource(generatedType));
        for (TableColumnSpec column : table.columns) {
            source.append(resourceKeySource(column.headerConstant, column.headerId));
            if (column.tooltipConstant != null) {
                source.append(resourceKeySource(column.tooltipConstant, column.tooltipId));
            }
        }
        source.append("}\n");
        writeSource(tableType, qualifiedName, source.toString(), "Could not write generated table resource keys");
    }

    private void writeColumnsClass(TypeElement tableType, TableSpec table) {
        String packageName = elements.getPackageOf(tableType).getQualifiedName().toString();
        String generatedType = table.ownerType + "Columns";
        String resourcesType = table.ownerType + "TableResources";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.core.key.HelpTopic;
                import cz.auderis.corusco.core.table.Column;
                import cz.auderis.corusco.core.table.ColumnCapabilities;
                import cz.auderis.corusco.core.table.ColumnDefaults;
                import cz.auderis.corusco.core.table.ColumnDescriptor;
                import cz.auderis.corusco.core.table.ColumnKey;
                import cz.auderis.corusco.core.table.ColumnPersistence;
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
            source.append(columnSource(table, resourcesType, column));
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

    private void writeTableBindingsClass(TypeElement tableType, TableSpec table) {
        String packageName = elements.getPackageOf(tableType).getQualifiedName().toString();
        String generatedType = table.ownerType + "TableBindings";
        String descriptorType = table.ownerType + "TableDescriptor";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        StringBuilder source = sourceBuilder(packageName);
        source.append("""
                import cz.auderis.corusco.core.collection.ObservableList;
                import cz.auderis.corusco.core.value.WritableValue;
                import cz.auderis.corusco.swing.binding.BindingScope;
                import cz.auderis.corusco.swing.table.ObservableTableModel;
                import cz.auderis.corusco.swing.table.TableSelectionBinding;
                import javax.swing.JTable;

                /**
                 * Generated table binding helpers for {@link %s}.
                 */
                public final class %s {

                """.formatted(table.ownerType, generatedType));
        source.append(privateConstructorSource(generatedType));
        source.append("""
                    /**
                     * Creates, installs, and scopes the generated table model.
                     *
                     * @param table Swing table receiving the generated model
                     * @param rows observable row source
                     * @param scope owner for model cleanup
                     * @return installed table model
                     */
                    public static ObservableTableModel<%s> installModel(
                            JTable table,
                            ObservableList<%s> rows,
                            BindingScope scope
                    ) {
                        ObservableTableModel<%s> model = %s.tableModel(rows);
                        table.setModel(model);
                        scope.add(model);
                        return model;
                    }

                    /**
                     * Binds table selection to a selected model-row value.
                     *
                     * @param table Swing table
                     * @param model generated table model installed on the table
                     * @param selectedModelRow selected model-row value
                     * @param scope owner for binding cleanup
                     * @return selection binding
                     */
                    public static TableSelectionBinding<%s> bindSelection(
                            JTable table,
                            ObservableTableModel<%s> model,
                            WritableValue<Integer> selectedModelRow,
                            BindingScope scope
                    ) {
                        return scope.add(TableSelectionBinding.bind(table, model, selectedModelRow));
                    }

                    /**
                     * Binds table selection to selected model-row and row values.
                     *
                     * @param table Swing table
                     * @param model generated table model installed on the table
                     * @param selectedModelRow selected model-row value
                     * @param selectedRow selected row value
                     * @param scope owner for binding cleanup
                     * @return selection binding
                     */
                    public static TableSelectionBinding<%s> bindSelection(
                            JTable table,
                            ObservableTableModel<%s> model,
                            WritableValue<Integer> selectedModelRow,
                            WritableValue<%s> selectedRow,
                            BindingScope scope
                    ) {
                        return scope.add(TableSelectionBinding.bind(table, model, selectedModelRow, selectedRow));
                    }
                }
                """.formatted(
                table.ownerType,
                table.ownerType,
                table.ownerType,
                descriptorType,
                table.ownerType,
                table.ownerType,
                table.ownerType,
                table.ownerType,
                table.ownerType
        ));
        writeSource(tableType, qualifiedName, source.toString(), "Could not write generated table bindings");
    }

    private String columnSource(TableSpec table, String resourcesType, TableColumnSpec column) {
        String tooltip = column.tooltipConstant == null ? "null" : resourcesType + "." + column.tooltipConstant;
        String helpTopic = column.helpTopicId == null ? "null" : "HelpTopic.of(" + stringLiteralOrNull(column.helpTopicId) + ")";
        String columnFactory = column.editable ? editableColumnSource(column) : readOnlyColumnSource(column);
        return """
                    /**
                     * Column key for {@code %s}.
                     */
                    public static final ColumnKey<%s, %s> %s_KEY =
                            ColumnKey.of(%s, %s.class, %s);

                    /**
                     * Column descriptor for {@code %s}.
                     */
                    public static final ColumnDescriptor<%s, %s> %s_DESCRIPTOR =
                            new ColumnDescriptor<>(
                                    %s_KEY,
                                    %s.%s,
                                    %s,
                                    %s,
                                    ColumnPersistence.of(%s, %d, %s),
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
                column.keyId,
                column.ownerType,
                column.valueType,
                column.constantName,
                column.constantName,
                resourcesType,
                column.headerConstant,
                tooltip,
                helpTopic,
                stringLiteralOrNull(column.persistenceId),
                column.minWidth,
                intLiteral(column.maxWidth),
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

    private static String resourceKeySource(String constantName, String resourceId) {
        return """
                    /**
                     * Resource key for {@code %s}.
                     */
                    public static final ResourceKey<String> %s =
                            ResourceKey.of(%s, String.class);

                """.formatted(resourceId, constantName, stringLiteralOrNull(resourceId));
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

    private static String intLiteral(int value) {
        return value == Integer.MAX_VALUE ? "Integer.MAX_VALUE" : Integer.toString(value);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
