package cz.auderis.corusco.core.table;

import java.util.Objects;

/**
 * Typed identity for one column in a table row type.
 *
 * <p>The stable id is suitable for generated constants, persistence, resource
 * lookup, and diagnostics. It deliberately does not describe how to access a
 * row value; {@link Column} carries the typed extractor/updater functions.</p>
 *
 * <p>Generated {@code @SwingTable} records create a {@code ColumnKey} constant
 * in {@code <Row>Columns} for each {@code @Column} record component.
 * Handwritten descriptors may create keys with {@link #of(String, Class,
 * Class)}.</p>
 *
 * @param id stable non-blank column id
 * @param rowType table row type
 * @param valueType reference cell value type
 * @param <R> row type
 * @param <V> cell value type
 */
public record ColumnKey<R, V>(String id, Class<R> rowType, Class<V> valueType) {

    /**
     * Creates a column key.
     *
     * @param id stable non-blank column id
     * @param rowType table row type
 * @param valueType reference cell value type
     */
    public ColumnKey {
        id = TableIds.requireId(id);
        Objects.requireNonNull(rowType, "rowType");
        Objects.requireNonNull(valueType, "valueType");
        if (rowType.isPrimitive()) {
            throw new IllegalArgumentException("rowType must not be primitive");
        }
        if (valueType.isPrimitive()) {
            throw new IllegalArgumentException("valueType must not be primitive");
        }
    }

    /**
     * Creates a column key for handwritten or generated-style code.
     *
     * @param id stable non-blank column id
     * @param rowType table row type
     * @param valueType reference cell value type
     * @param <R> row type
     * @param <V> cell value type
     * @return column key
     */
    public static <R, V> ColumnKey<R, V> of(String id, Class<R> rowType, Class<V> valueType) {
        return new ColumnKey<>(id, rowType, valueType);
    }

    @Override
    public String toString() {
        return "ColumnKey[" + rowType.getSimpleName() + "#" + id + ":" + valueType.getSimpleName() + "]";
    }
}
