package cz.auderis.corusco.annotations.dataset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record component as the primary time axis.
 *
 * <p>A time axis gives a descriptor temporal semantics. It allows time-range
 * request validation and gives adapters a canonical column for downsampling or
 * ordering. A data set can have at most one time axis.</p>
 *
 * <p>The current generated-frame stage records the unit and monotonic hint but
 * does not enforce ordering. Adapters that rely on monotonic input should still
 * validate or document their assumptions at execution boundaries.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.RECORD_COMPONENT)
public @interface TimeAxis {

    /**
     * Stable column id or blank for the default derived from the data-set id
     * and record-component name.
     *
     * @return stable id or blank
     */
    String id() default "";

    /**
     * Time-axis unit id or symbol stored in the generated descriptor.
     *
     * <p>Typical values are {@code millis}, {@code micros}, {@code nanos}, or
     * application-specific clock identifiers. Core treats the value as metadata
     * and does not convert timestamps.</p>
     *
     * @return unit id
     */
    String unit() default "millis";

    /**
     * Whether the source is expected to be monotonic by this axis.
     *
     * <p>This is a schema hint for generated storage and adapters. It does not
     * make the processor sort or validate input rows.</p>
     *
     * @return true when monotonic
     */
    boolean monotonic() default false;
}
