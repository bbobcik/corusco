package cz.auderis.corusco.annotations.dataset;

/**
 * Standard aggregation functions that can be requested for fixed-schema data.
 *
 * <p>The enum describes transport-neutral intent only. Data-set annotations
 * use it to declare which operations make sense for a measure column, and core
 * aggregation requests use it to name the operation a caller wants. The actual
 * execution belongs to an adapter, generated frame implementation, query
 * planner, or application service.</p>
 *
 * <p>Implementations should document their null and missing-value behavior for
 * every supported function. For example, one adapter may ignore values marked
 * missing, while another may preserve a separate quality result.</p>
 */
public enum AggregationFunction {

    /**
     * First value in descriptor or adapter-defined order.
     */
    FIRST,

    /**
     * Last value in descriptor or adapter-defined order.
     */
    LAST,

    /**
     * Minimum value according to the adapter's ordering for the value type.
     */
    MIN,

    /**
     * Maximum value according to the adapter's ordering for the value type.
     */
    MAX,

    /**
     * Sum of values in the selected group or time bucket.
     */
    SUM,

    /**
     * Count of rows or values, as defined by the executing adapter.
     */
    COUNT,

    /**
     * Arithmetic mean of values in the selected group or time bucket.
     */
    AVERAGE,

    /**
     * Weighted mean. The weight column or weighting rule is adapter-specific.
     */
    WEIGHTED_AVERAGE,

    /**
     * Application-defined aggregation outside the standard vocabulary.
     */
    CUSTOM
}
