package cz.auderis.corusco.annotations.dataset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a fixed-schema data-set column when no narrower role annotation fits.
 *
 * <p>Prefer {@link TimeAxis}, {@link Dimension}, {@link Measure}, or
 * {@link QualityColumn} when the component has one of those common meanings.
 * This annotation exists for sequence, metadata, auxiliary, and other explicit
 * roles that still need to participate in generated data-set descriptors and
 * generated columnar frames.</p>
 *
 * <p>The role, missing-value policy, quality policy, and aggregation functions
 * are the same core concepts used by generated descriptors. Keeping the
 * annotation attributes typed with core dataset enums prevents annotation and
 * runtime metadata from drifting apart.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.RECORD_COMPONENT)
public @interface DataColumn {

    /**
     * Stable column id. When blank, the processor derives one from the
     * data-set id and record-component name.
     *
     * @return stable id or blank
     */
    String id() default "";

    /**
     * Semantic column role used in the generated descriptor.
     *
     * @return role
     */
    DataColumnRole role() default DataColumnRole.AUXILIARY;

    /**
     * Unit id or symbol for values in this column.
     *
     * <p>The value is copied into generated {@code UnitMetadata}; Corusco does
     * not interpret or convert units in the annotation processor.</p>
     *
     * @return unit id or blank
     */
    String unit() default "";

    /**
     * Missing-value policy declared for this column.
     *
     * <p>{@link MissingPolicy#NAN} is accepted only for {@code float},
     * {@code double}, {@link Float}, and {@link Double} components.</p>
     *
     * @return missing-value policy
     */
    MissingPolicy missing() default MissingPolicy.NONE;

    /**
     * Quality policy declared for this column.
     *
     * @return quality policy
     */
    QualityPolicy quality() default QualityPolicy.NONE;

    /**
     * Allowed aggregation functions for this column.
     *
     * <p>Aggregation functions are valid only when {@link #role()} is
     * {@link DataColumnRole#MEASURE}. The processor rejects aggregation
     * metadata on non-measure columns.</p>
     *
     * @return aggregation functions
     */
    AggregationFunction[] aggregations() default {};
}
