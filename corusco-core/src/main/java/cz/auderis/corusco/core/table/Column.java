package cz.auderis.corusco.core.table;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Executable table column definition.
 *
 * <p>A column combines immutable descriptor metadata with typed row access.
 * Read access is an explicit function, not reflection or a string property
 * path. Editable columns use an updater function that returns the replacement
 * row, which fits immutable record-row tables and generated wither methods.</p>
 *
 * @param <R> row type
 * @param <V> cell value type
 */
public final class Column<R, V> {

    private final ColumnDescriptor<R, V> descriptor;
    private final Function<? super R, ? extends V> getter;
    private final BiFunction<? super R, ? super V, ? extends R> updater;

    private Column(
            ColumnDescriptor<R, V> descriptor,
            Function<? super R, ? extends V> getter,
            BiFunction<? super R, ? super V, ? extends R> updater
    ) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.getter = Objects.requireNonNull(getter, "getter");
        this.updater = updater;
        if (descriptor.capabilities().editable() && updater == null) {
            throw new IllegalArgumentException("editable columns require an updater");
        }
        if (!descriptor.capabilities().editable() && updater != null) {
            throw new IllegalArgumentException("read-only column cannot declare an updater");
        }
    }

    /**
     * Creates a read-only column.
     *
     * @param descriptor column descriptor
     * @param getter typed row value extractor
     * @param <R> row type
     * @param <V> cell value type
     * @return read-only column
     */
    public static <R, V> Column<R, V> readOnly(
            ColumnDescriptor<R, V> descriptor,
            Function<? super R, ? extends V> getter
    ) {
        return new Column<>(descriptor, getter, null);
    }

    /**
     * Creates an editable column.
     *
     * @param descriptor column descriptor with editable capabilities
     * @param getter typed row value extractor
     * @param updater row updater returning the replacement row
     * @param <R> row type
     * @param <V> cell value type
     * @return editable column
     */
    public static <R, V> Column<R, V> editable(
            ColumnDescriptor<R, V> descriptor,
            Function<? super R, ? extends V> getter,
            BiFunction<? super R, ? super V, ? extends R> updater
    ) {
        return new Column<>(descriptor, getter, updater);
    }

    /**
     * Returns column metadata.
     *
     * @return descriptor
     */
    public ColumnDescriptor<R, V> descriptor() {
        return descriptor;
    }

    /**
     * Returns this column's typed key.
     *
     * @return column key
     */
    public ColumnKey<R, V> key() {
        return descriptor.key();
    }

    /**
     * Returns the cell value type.
     *
     * @return value type
     */
    public Class<V> valueType() {
        return key().valueType();
    }

    /**
     * Returns whether this column can update rows.
     *
     * @return true when editable
     */
    public boolean editable() {
        return updater != null;
    }

    /**
     * Extracts the cell value from a row.
     *
     * @param row source row
     * @return cell value
     */
    public V value(R row) {
        return getter.apply(Objects.requireNonNull(row, "row"));
    }

    /**
     * Returns a replacement row with the supplied cell value.
     *
     * @param row source row
     * @param value new cell value
     * @return replacement row
     */
    public R update(R row, Object value) {
        if (updater == null) {
            throw new UnsupportedOperationException("Column is not editable: " + key().id());
        }
        return updater.apply(Objects.requireNonNull(row, "row"), valueType().cast(value));
    }
}
