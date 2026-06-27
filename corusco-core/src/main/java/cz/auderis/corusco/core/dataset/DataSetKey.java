package cz.auderis.corusco.core.dataset;

import java.util.Objects;

/**
 * Typed identity for a fixed-schema data set.
 *
 * <p>The id is the stable schema id used in generated descriptor constants,
 * diagnostics, request payloads, and adapter mappings. It is independent from
 * Java package names and from presentation table ids, although applications may
 * choose to keep those ids aligned for readability.</p>
 *
 * <p>The row type is the record or class that generated the data-set schema.
 * It makes keys type-aware so a column key or descriptor cannot accidentally
 * be mixed across unrelated row models without an explicit generic escape.</p>
 *
 * @param id stable non-blank data-set id
 * @param rowType source row type
 * @param <R> source row type
 */
public record DataSetKey<R>(String id, Class<R> rowType) {

    /**
     * Creates a key.
     *
     * @param id stable id containing letters, digits, dots, underscores,
     *        dashes, or slashes
     * @param rowType source row type; primitive classes are rejected because
     *        generated data-set sources are record/class based
     */
    public DataSetKey {
        id = DataSetIds.requireId(id);
        Objects.requireNonNull(rowType, "rowType");
        if (rowType.isPrimitive()) {
            throw new IllegalArgumentException("rowType must not be primitive");
        }
    }

    /**
     * Creates a key.
     *
     * @param id stable id containing letters, digits, dots, underscores,
     *        dashes, or slashes
     * @param rowType source row type
     * @param <R> source row type
     * @return key
     */
    public static <R> DataSetKey<R> of(String id, Class<R> rowType) {
        return new DataSetKey<>(id, rowType);
    }

    @Override
    public String toString() {
        return "DataSetKey[" + rowType.getSimpleName() + "#" + id + "]";
    }
}
