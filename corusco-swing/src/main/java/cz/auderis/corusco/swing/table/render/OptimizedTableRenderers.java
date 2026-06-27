package cz.auderis.corusco.swing.table.render;

import cz.auderis.corusco.core.dataset.DataColumnDescriptor;
import cz.auderis.corusco.core.dataset.DataColumnKey;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.table.DataSetFrameTableModel;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import java.util.Objects;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Installs optional optimized renderers into Swing tables.
 *
 * <p>This utility is the public entry point for the renderer package. It keeps
 * renderer selection explicit: callers choose a table, choose either a value
 * type or a descriptor-backed column, and supply renderer options. The methods
 * mutate Swing renderer configuration immediately and return a {@link Binding}
 * that restores the previous renderer when closed.</p>
 *
 * <p>Default-renderer installation follows Swing's normal
 * {@link JTable#setDefaultRenderer(Class, TableCellRenderer)} semantics. It
 * affects all cells in the table whose column class resolves to the supplied
 * value type unless a {@link TableColumn} has its own renderer. Column
 * installation targets one currently visible column of an
 * {@link ObservableTableModel} by {@link ColumnKey}; it is useful when a value
 * type such as {@link Long} appears in several columns but only one represents
 * an epoch timestamp.</p>
 *
 * <p>All methods must be called on the Swing Event Dispatch Thread. The
 * returned binding must also be closed on the EDT. Renderer instances created
 * here are owned by the table configuration they are installed into and should
 * not be reused elsewhere.</p>
 *
 * @see TimestampRendererOptions
 * @see StateRendererOptions
 */
public final class OptimizedTableRenderers {

    private OptimizedTableRenderers() {
    }

    /**
     * Installs an epoch timestamp renderer as the default renderer for a value
     * type.
     *
     * <p>The method stores the table's current default renderer for
     * {@code valueType}, replaces it with a timestamp renderer, and returns a
     * binding that restores the stored renderer. Closing the returned binding is
     * idempotent.</p>
     *
     * @param table target table, not {@code null}
     * @param valueType value class rendered by the timestamp renderer, not
     *        {@code null}
     * @param options renderer options, not {@code null}
     * @return binding that restores the previous default renderer
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalStateException if called off the EDT
     */
    public static Binding installTimestampRenderer(
            JTable table,
            Class<?> valueType,
            TimestampRendererOptions options
    ) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(valueType, "valueType");
        Objects.requireNonNull(options, "options");
        TableCellRenderer original = table.getDefaultRenderer(valueType);
        table.setDefaultRenderer(valueType, new TimestampTableCellRenderer(options));
        return RendererBinding.defaultRenderer(table, valueType, original);
    }

    /**
     * Installs an epoch timestamp renderer on one currently visible model
     * column.
     *
     * <p>The supplied table must currently use {@code model}. The column is
     * resolved by comparing descriptor column keys, then matched to the
     * table's visible {@link TableColumn} by model index. Reordered columns are
     * handled correctly. Hidden columns are rejected because Swing has no
     * visible column renderer slot to mutate at installation time.</p>
     *
     * @param table target table, not {@code null}
     * @param model descriptor-backed table model installed on the table, not
     *        {@code null}
     * @param columnKey target column key, not {@code null}
     * @param options renderer options, not {@code null}
     * @param <R> row type
     * @return binding that restores the previous column renderer
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if the table does not use {@code model},
     *         the key is not part of the descriptor, or the column is hidden
     * @throws IllegalStateException if called off the EDT
     */
    public static <R> Binding installTimestampRenderer(
            JTable table,
            ObservableTableModel<R> model,
            ColumnKey<R, ?> columnKey,
            TimestampRendererOptions options
    ) {
        return installColumnRenderer(table, model, columnKey, new TimestampTableCellRenderer(options));
    }

    /**
     * Installs an epoch timestamp renderer on one currently visible data-set
     * model column.
     *
     * @param table target table, not {@code null}
     * @param model data-set table model installed on the table, not {@code null}
     * @param columnKey target column key, not {@code null}
     * @param options renderer options, not {@code null}
     * @param <R> row type
     * @return binding that restores the previous column renderer
     */
    public static <R> Binding installTimestampRenderer(
            JTable table,
            DataSetFrameTableModel<R> model,
            DataColumnKey<R, ?> columnKey,
            TimestampRendererOptions options
    ) {
        return installColumnRenderer(table, model, columnKey, new TimestampTableCellRenderer(options));
    }

    /**
     * Installs a cached finite-state renderer as the default renderer for a
     * value type.
     *
     * <p>The method stores the table's current default renderer for
     * {@code valueType}, replaces it with a state renderer, and returns a
     * binding that restores the stored renderer. Closing the returned binding is
     * idempotent.</p>
     *
     * @param table target table, not {@code null}
     * @param valueType value class rendered by the state renderer, not
     *        {@code null}
     * @param options renderer options, not {@code null}
     * @return binding that restores the previous default renderer
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalStateException if called off the EDT
     */
    public static Binding installStateRenderer(
            JTable table,
            Class<?> valueType,
            StateRendererOptions options
    ) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(valueType, "valueType");
        Objects.requireNonNull(options, "options");
        TableCellRenderer original = table.getDefaultRenderer(valueType);
        table.setDefaultRenderer(valueType, new StateTableCellRenderer(options));
        return RendererBinding.defaultRenderer(table, valueType, original);
    }

    /**
     * Installs a cached finite-state renderer on one currently visible model
     * column.
     *
     * <p>The supplied table must currently use {@code model}. The column is
     * resolved by descriptor key rather than by visible index or header text,
     * so user column reordering does not change which model column receives the
     * renderer. Hidden columns are rejected because there is no current
     * {@link TableColumn} to restore later.</p>
     *
     * @param table target table, not {@code null}
     * @param model descriptor-backed table model installed on the table, not
     *        {@code null}
     * @param columnKey target column key, not {@code null}
     * @param options renderer options, not {@code null}
     * @param <R> row type
     * @return binding that restores the previous column renderer
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if the table does not use {@code model},
     *         the key is not part of the descriptor, or the column is hidden
     * @throws IllegalStateException if called off the EDT
     */
    public static <R> Binding installStateRenderer(
            JTable table,
            ObservableTableModel<R> model,
            ColumnKey<R, ?> columnKey,
            StateRendererOptions options
    ) {
        return installColumnRenderer(table, model, columnKey, new StateTableCellRenderer(options));
    }

    private static <R> Binding installColumnRenderer(
            JTable table,
            ObservableTableModel<R> model,
            ColumnKey<R, ?> columnKey,
            TableCellRenderer renderer
    ) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(columnKey, "columnKey");
        Objects.requireNonNull(renderer, "renderer");
        if (table.getModel() != model) {
            throw new IllegalArgumentException("table must use the supplied ObservableTableModel");
        }
        int modelIndex = modelIndex(model, columnKey);
        TableColumn tableColumn = visibleTableColumn(table, modelIndex);
        TableCellRenderer original = tableColumn.getCellRenderer();
        tableColumn.setCellRenderer(renderer);
        return RendererBinding.columnRenderer(tableColumn, original);
    }

    private static <R> Binding installColumnRenderer(
            JTable table,
            DataSetFrameTableModel<R> model,
            DataColumnKey<R, ?> columnKey,
            TableCellRenderer renderer
    ) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(columnKey, "columnKey");
        Objects.requireNonNull(renderer, "renderer");
        if (table.getModel() != model) {
            throw new IllegalArgumentException("table must use the supplied DataSetFrameTableModel");
        }
        int modelIndex = modelIndex(model, columnKey);
        TableColumn tableColumn = visibleTableColumn(table, modelIndex);
        TableCellRenderer original = tableColumn.getCellRenderer();
        tableColumn.setCellRenderer(renderer);
        return RendererBinding.columnRenderer(tableColumn, original);
    }

    private static <R> int modelIndex(ObservableTableModel<R> model, ColumnKey<R, ?> columnKey) {
        for (int i = 0; i < model.descriptor().columns().size(); i++) {
            Column<R, ?> column = model.descriptor().column(i);
            if (column.key().equals(columnKey)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown column key: " + columnKey);
    }

    private static <R> int modelIndex(DataSetFrameTableModel<R> model, DataColumnKey<R, ?> columnKey) {
        for (int i = 0; i < model.descriptor().columns().size(); i++) {
            DataColumnDescriptor<R, ?> column = model.descriptor().columns().get(i);
            if (column.key().equals(columnKey)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown data column key: " + columnKey);
    }

    private static TableColumn visibleTableColumn(JTable table, int modelIndex) {
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            if (column.getModelIndex() == modelIndex) {
                return column;
            }
        }
        throw new IllegalArgumentException("Column is not currently visible: model index " + modelIndex);
    }
}
