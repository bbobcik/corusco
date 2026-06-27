package cz.auderis.corusco.core.data;

/**
 * Primitive total-row count state for a data query.
 *
 * <p>Large or remote data sources cannot always provide an exact total count
 * at the same time as the first page. This record separates the precision of a
 * count from its numeric value without using boxed {@link Long} or
 * {@code Optional} in repeated page/status objects. Unknown counts always use
 * value {@code 0}; estimate and exact counts carry a non-negative primitive
 * value.</p>
 *
 * <p>A presenter can use this distinction directly: unknown counts may hide a
 * page total, estimated counts may display an approximation marker, and exact
 * counts may enable last-page navigation. The record does not prescribe any
 * visual wording.</p>
 *
 * @param kind count precision
 * @param value count value for exact or estimated counts; zero for unknown
 */
public record CoruscoDataCount(Kind kind, long value) {

    /**
     * Count precision.
     *
     * <p>The precision determines whether {@link CoruscoDataCount#value()} is
     * meaningful and how conservative navigation or status text should be.</p>
     */
    public enum Kind {
        /**
         * Total count is not known.
         */
        UNKNOWN,
        /**
         * Count is approximate.
         */
        ESTIMATE,
        /**
         * Count is exact.
         */
        EXACT
    }

    /**
     * Unknown count.
     *
     * <p>This constant is safe to reuse because the record is immutable.</p>
     */
    public static final CoruscoDataCount UNKNOWN = new CoruscoDataCount(Kind.UNKNOWN, 0L);

    /**
     * Creates a count.
     *
     * <p>Unknown counts must use value zero so callers do not accidentally
     * interpret a hidden stale value. Estimated and exact counts must be
     * non-negative.</p>
     *
     * @param kind count precision
     * @param value count value
     */
    public CoruscoDataCount {
        java.util.Objects.requireNonNull(kind, "kind");
        if (value < 0L) {
            throw new IllegalArgumentException("value must not be negative");
        }
        if (kind == Kind.UNKNOWN && value != 0L) {
            throw new IllegalArgumentException("unknown count must use value zero");
        }
    }

    /**
     * Creates an exact count.
     *
     * <p>Use exact counts when the backing store has counted the whole result
     * set for the current query.</p>
     *
     * @param value exact count
     * @return count
     */
    public static CoruscoDataCount exact(long value) {
        return new CoruscoDataCount(Kind.EXACT, value);
    }

    /**
     * Creates an estimated count.
     *
     * <p>Use estimated counts when the value is useful for progress or paging
     * but may change after the backend performs a more expensive count.</p>
     *
     * @param value estimated count
     * @return count
     */
    public static CoruscoDataCount estimate(long value) {
        return new CoruscoDataCount(Kind.ESTIMATE, value);
    }

    /**
     * Indicates whether the count has a usable numeric value.
     *
     * @return true for estimate and exact counts
     */
    public boolean hasValue() {
        return kind != Kind.UNKNOWN;
    }
}
