package cz.auderis.corusco.core.table;

import java.util.Objects;

/**
 * Typed identity for a table over rows of type {@code R}.
 *
 * <p>The id is a stable boundary token for generated code, persistence,
 * diagnostics, and resource lookup. It is not a JavaBeans property path or a
 * user-facing title. Equality includes both the id and row type.</p>
 *
 * <p>Generated {@code @SwingTable} records create a {@code TableKey} constant
 * named {@code TABLE} in a row-specific columns companion, for example
 * {@code CustomerRowColumns}. Handwritten table descriptors
 * may create keys with {@link #of(String, Class)}.</p>
 *
 * @param id stable non-blank table id
 * @param rowType table row type
 * @param <R> row type
 */
public record TableKey<R>(String id, Class<R> rowType) {

    /**
     * Creates a table key.
     *
     * @param id stable non-blank table id
     * @param rowType table row type
     */
    public TableKey {
        id = TableIds.requireId(id);
        Objects.requireNonNull(rowType, "rowType");
        if (rowType.isPrimitive()) {
            throw new IllegalArgumentException("rowType must not be primitive");
        }
    }

    /**
     * Creates a table key for handwritten or generated-style code.
     *
     * @param id stable non-blank table id
     * @param rowType table row type
     * @param <R> row type
     * @return table key
     */
    public static <R> TableKey<R> of(String id, Class<R> rowType) {
        return new TableKey<>(id, rowType);
    }

    @Override
    public String toString() {
        return "TableKey[" + rowType.getSimpleName() + "#" + id + "]";
    }
}
