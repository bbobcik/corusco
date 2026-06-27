package cz.auderis.corusco.annotations.dataset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record component as a measured value column.
 *
 * <p>Measures are numeric or otherwise aggregatable observations such as
 * prices, volumes, counters, durations, and physical measurements. Generated
 * data-set descriptors expose measure columns separately so request builders
 * and adapters can distinguish values that can be aggregated from dimensions
 * that identify a series, group, or state.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.RECORD_COMPONENT)
public @interface Measure {

    /**
     * Stable column id or blank for the default.
     *
     * @return stable id or blank
     */
    String id() default "";

    /**
     * Unit id or symbol copied to the generated descriptor.
     *
     * <p>Use a stable identifier when adapters need machine-readable metadata
     * and a familiar symbol when the unit is purely descriptive.</p>
     *
     * @return unit id or blank
     */
    String unit() default "";

    /**
     * Missing-value policy for the measure.
     *
     * <p>{@link MissingPolicy#NAN} is accepted only for floating-point
     * components. Nullable reference measures normally use
     * {@link MissingPolicy#NULL_VALUE}.</p>
     *
     * @return missing-value policy
     */
    MissingPolicy missing() default MissingPolicy.NONE;

    /**
     * Quality policy for the measure values.
     *
     * <p>Use this only when the measure value itself carries quality state.
     * Prefer {@link QualityColumn} when quality state is represented by a
     * separate component.</p>
     *
     * @return quality policy
     */
    QualityPolicy quality() default QualityPolicy.NONE;

    /**
     * Aggregation functions that generated request models may ask adapters to
     * execute for this measure.
     *
     * @return aggregation functions
     */
    AggregationFunction[] aggregations() default {};
}
