package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.dataset.DataColumnDescriptor;
import cz.auderis.corusco.core.dataset.DataSetDescriptor;
import cz.auderis.corusco.swing.binding.Binding;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.IntSupplier;
import javax.swing.table.AbstractTableModel;

/**
 * Read-only Swing table model over a fixed-schema data-set frame.
 *
 * <p>The model is a bridge from semantic data-set descriptors to ordinary
 * {@code JTable} rendering. It owns no data and no generated storage. Callers
 * provide the row count and value lookup, usually by delegating to a generated
 * {@code *Frame}. The descriptor remains available so renderers, exporters, or
 * tooltips can inspect units, missing-value policy, quality policy, and
 * column roles instead of treating the table column as the schema authority.</p>
 *
 * @param <R> source row type
 */
public final class DataSetFrameTableModel<R> extends AbstractTableModel implements Binding {

    private static final long serialVersionUID = -3327229826275534307L;

    private final DataSetDescriptor<R> descriptor;
    private final IntSupplier rowCount;
    private final BiFunction<Integer, DataColumnDescriptor<R, ?>, Object> valueProvider;

    /**
     * Creates a table model.
     *
     * @param descriptor data-set descriptor
     * @param rowCount row-count provider
     * @param valueProvider value provider by row and descriptor column
     */
    public DataSetFrameTableModel(
            DataSetDescriptor<R> descriptor,
            IntSupplier rowCount,
            BiFunction<Integer, DataColumnDescriptor<R, ?>, Object> valueProvider
    ) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.rowCount = Objects.requireNonNull(rowCount, "rowCount");
        this.valueProvider = Objects.requireNonNull(valueProvider, "valueProvider");
    }

    /**
     * Creates a table model.
     *
     * @param descriptor data-set descriptor
     * @param rowCount row-count provider
     * @param valueProvider value provider by row and descriptor column
     * @param <R> source row type
     * @return table model
     */
    public static <R> DataSetFrameTableModel<R> of(
            DataSetDescriptor<R> descriptor,
            IntSupplier rowCount,
            BiFunction<Integer, DataColumnDescriptor<R, ?>, Object> valueProvider
    ) {
        return new DataSetFrameTableModel<>(descriptor, rowCount, valueProvider);
    }

    @Override
    public int getRowCount() {
        return rowCount.getAsInt();
    }

    @Override
    public int getColumnCount() {
        return descriptor.columns().size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return column(columnIndex).key().id();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return column(columnIndex).key().valueType();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= getRowCount()) {
            throw new IndexOutOfBoundsException("row=" + rowIndex + ", size=" + getRowCount());
        }
        return valueProvider.apply(rowIndex, column(columnIndex));
    }

    /**
     * Returns the semantic descriptor used by this table model.
     *
     * @return descriptor
     */
    public DataSetDescriptor<R> descriptor() {
        return descriptor;
    }

    /**
     * Returns the semantic descriptor for a model column.
     *
     * @param columnIndex model column index
     * @return column descriptor
     */
    public DataColumnDescriptor<R, ?> column(int columnIndex) {
        return descriptor.columns().get(columnIndex);
    }

    @Override
    public void close() {
        // The bridge owns no subscriptions or storage.
    }
}
