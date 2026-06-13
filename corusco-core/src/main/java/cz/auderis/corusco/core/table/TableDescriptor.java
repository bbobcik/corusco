package cz.auderis.corusco.core.table;

import java.util.List;
import java.util.Objects;

/**
 * Immutable descriptor for a typed table.
 *
 * <p>The descriptor preserves generated column order and keeps table identity
 * next to executable columns. Later generated descriptor classes should expose
 * constants of this shape rather than requiring callers to assemble tables from
 * strings.</p>
 *
 * @param key typed table key
 * @param columns immutable ordered columns
 * @param <R> row type
 */
public record TableDescriptor<R>(TableKey<R> key, List<Column<R, ?>> columns) {

    /**
     * Creates table metadata.
     *
     * @param key typed table key
     * @param columns ordered columns
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
     * Returns column at index.
     *
     * @param index column index
     * @return column
     */
    public Column<R, ?> column(int index) {
        return columns.get(index);
    }
}
