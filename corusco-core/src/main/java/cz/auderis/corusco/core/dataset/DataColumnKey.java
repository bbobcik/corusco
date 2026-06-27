package cz.auderis.corusco.core.dataset;

import java.util.Objects;

/**
 * Typed identity for one column in a fixed-schema data set.
 *
 * <p>The id is a stable schema id suitable for generated constants,
 * diagnostics, request models, adapter translation, and generated frame
 * access. It is independent from external wire names and from Swing table
 * persistence ids.</p>
 *
 * <p>The owning data-set key and value type are part of equality. This keeps
 * columns from different row models distinct even when their ids happen to be
 * the same, and it lets renderers or adapters inspect the reference value type
 * without consulting generated accessor code.</p>
 *
 * @param id stable non-blank column id
 * @param dataSetKey owning data-set key
 * @param valueType reference value type
 * @param <R> source row type
 * @param <V> value type
 */
public record DataColumnKey<R, V>(String id, DataSetKey<R> dataSetKey, Class<V> valueType) {

    /**
     * Creates a key.
     *
     * @param id stable id containing letters, digits, dots, underscores,
     *        dashes, or slashes
     * @param dataSetKey owning data-set key
     * @param valueType reference value type; primitive classes are rejected so
     *        generated primitive columns still expose boxed table/request
     *        value types consistently
     */
    public DataColumnKey {
        id = DataSetIds.requireId(id);
        Objects.requireNonNull(dataSetKey, "dataSetKey");
        Objects.requireNonNull(valueType, "valueType");
        if (valueType.isPrimitive()) {
            throw new IllegalArgumentException("valueType must not be primitive");
        }
    }

    /**
     * Creates a key.
     *
     * @param id stable id containing letters, digits, dots, underscores,
     *        dashes, or slashes
     * @param dataSetKey owning data-set key
     * @param valueType reference value type
     * @param <R> source row type
     * @param <V> value type
     * @return key
     */
    public static <R, V> DataColumnKey<R, V> of(String id, DataSetKey<R> dataSetKey, Class<V> valueType) {
        return new DataColumnKey<>(id, dataSetKey, valueType);
    }

    @Override
    public String toString() {
        return "DataColumnKey[" + dataSetKey.rowType().getSimpleName() + "#" + id + ":"
                + valueType.getSimpleName() + "]";
    }
}
