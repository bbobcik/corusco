package cz.auderis.corusco.annotations.dataset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record component as a dimension column.
 *
 * <p>Dimensions identify, group, partition, or label observations. Typical
 * examples are symbols, venues, regions, device ids, states, and categories.
 * Generated descriptors expose dimensions separately from measures so adapters
 * can build grouping, filtering, and series-selection UIs without inferring
 * intent from Java types or presentation table columns.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.RECORD_COMPONENT)
public @interface Dimension {

    /**
     * Stable column id or blank for the default.
     *
     * @return stable id or blank
     */
    String id() default "";

    /**
     * Missing-value policy for the dimension value.
     *
     * <p>The default is {@link MissingPolicy#NULL_VALUE}, which fits nullable
     * reference dimensions. Primitive dimensions should normally override this
     * to {@link MissingPolicy#NONE} or a sentinel-oriented policy.</p>
     *
     * @return missing-value policy
     */
    MissingPolicy missing() default MissingPolicy.NULL_VALUE;
}
