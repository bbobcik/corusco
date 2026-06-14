package cz.auderis.corusco.core.table;

import java.util.List;
import java.util.Objects;

/**
 * Immutable descriptor for a typed table and its executable columns.
 *
 * <p>A table descriptor is the core, Swing-neutral description that generated
 * table metadata and Swing table adapters share. It combines the stable
 * {@link TableKey} for a row type with the ordered {@link Column} definitions
 * that know how to read and optionally update cell values. Swing code can turn
 * the descriptor into an {@code ObservableTableModel}; persistence code can
 * derive default {@link TableState}; generated resource keys can resolve
 * headers and tooltips from the column descriptors.</p>
 *
 * <p>The descriptor owns no row data and no UI state. Its column list is copied
 * at construction and must be non-empty. Each column key must refer to the same
 * row type as the table key, which protects generated descriptors and
 * handwritten table definitions from mixing unrelated row models. Keep table
 * and column ids stable when persisted state or tests depend on them.</p>
 *
 * @param key typed table key
 * @param columns immutable ordered columns
 * @param <R> row type
 */
public record TableDescriptor<R>(TableKey<R> key, List<Column<R, ?>> columns) {

    /**
     * Creates table metadata.
     *
     * <p>The column list is defensively copied. Later changes to the source
     * list are not observed.</p>
     *
     * @param key typed table key
     * @param columns ordered columns
     * @throws IllegalArgumentException if no columns are supplied or a column
     *         belongs to a different row type
     */
    public TableDescriptor {
        Objects.requireNonNull(key, "key");
        columns = List.copyOf(Objects.requireNonNull(columns, "columns"));
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("columns must not be empty");
        }
        for (Column<R, ?> column : columns) {
            if (!column.key().rowType().equals(key.rowType())) {
                throw new IllegalArgumentException("column row type does not match table row type: " + column.key());
            }
        }
    }

    /**
     * Returns the column at a model-column index.
     *
     * <p>The index follows descriptor order, which is also the JTable model
     * column order used by the Swing adapter. Visual column order may differ
     * after a Swing table-state controller restores user state.</p>
     *
     * @param index column index
     * @return column
     */
    public Column<R, ?> column(int index) {
        return columns.get(index);
    }
}
