package cz.auderis.corusco.core.table;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Executable definition of one column in a typed Corusco table.
 *
 * <p>A {@code Column} is where generated or handwritten table metadata becomes
 * usable by adapters such as Corusco's Swing observable table model. The
 * {@link ColumnDescriptor} supplies stable ids, resource keys, defaults,
 * persistence metadata, and capabilities; the column itself supplies the typed
 * functions that read a value from a row and, for editable columns, create the
 * replacement row after a cell edit.</p>
 *
 * <p>The class deliberately avoids reflection and string property paths. Read
 * access is a function supplied by generated code or handwritten descriptors.
 * Editable columns use an updater function that returns a new row, which fits
 * immutable records and generated wither-style methods. Instances are
 * immutable and reusable as descriptor constants; they do not own row data,
 * Swing components, table models, or persisted state.</p>
 *
 * <p>Use {@link #readOnly(ColumnDescriptor, Function)} for display-only
 * columns and {@link #editable(ColumnDescriptor, Function, BiFunction)} only
 * when the descriptor capabilities say the column is editable. Avoid using
 * mutable row side effects inside getters or updaters; table adapters assume
 * the returned value or replacement row represents the model change.</p>
 *
 * <p>Generated {@code @CoruscoTable} records create {@code Column} constants in
 * row-specific columns companions such as {@code CustomerRowColumns}. Read-only generated columns call the record accessor;
 * editable generated columns call the canonical record constructor to return a
 * replacement row with the edited value.</p>
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
     * <p>The descriptor must describe a non-editable column. The getter is
     * retained and invoked whenever an adapter needs a cell value.</p>
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
     * <p>The descriptor must declare editable capabilities. The updater is
     * retained and is called with the current row and a value cast to the
     * column's declared value type.</p>
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
     * Returns immutable descriptor metadata for this column.
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
     * <p>The supplied row must not be {@code null}. The getter's return value
     * may be {@code null} if the column's semantic value allows nulls.</p>
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
     * <p>Adapters call this only for editable columns. The value is cast to the
     * column's declared {@link #valueType()} before the updater is invoked. The
     * original row is not modified by this class; the updater decides whether
     * to return a copied immutable row or a mutated row object.</p>
     *
     * @param row source row
     * @param value new cell value
     * @return replacement row
     * @throws UnsupportedOperationException if this column is read-only
     * @throws ClassCastException if {@code value} is incompatible with the
     *         declared value type
     */
    public R update(R row, Object value) {
        if (updater == null) {
            throw new UnsupportedOperationException("Column is not editable: " + key().id());
        }
        return updater.apply(Objects.requireNonNull(row, "row"), valueType().cast(value));
    }
}
