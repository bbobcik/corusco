package cz.auderis.corusco.annotations.dataset;

/**
 * Semantic role of a fixed-schema data-set column.
 *
 * <p>Roles are the primary way generated descriptors communicate intent to
 * adapters. They are not Java type categories and they are not UI column
 * categories. A {@code String} can be a dimension, metadata, or auxiliary
 * column depending on how the application uses it; a {@code long} can be a
 * time axis, sequence, measure, or sentinel-coded dimension.</p>
 */
public enum DataColumnRole {

    /**
     * Ordered temporal axis used for time-range filtering and downsampling.
     */
    TIME_AXIS,

    /**
     * Value that identifies, partitions, groups, or labels observations.
     */
    DIMENSION,

    /**
     * Observed value that can be aggregated or summarized.
     */
    MEASURE,

    /**
     * Validity, provenance, confidence, or quality state for one or more
     * columns.
     */
    QUALITY,

    /**
     * Stable row sequence or ingestion order.
     */
    SEQUENCE,

    /**
     * Descriptive metadata that should travel with the row but is not normally
     * used as a dimension or measure.
     */
    METADATA,

    /**
     * Supporting column with application-specific meaning.
     */
    AUXILIARY
}
