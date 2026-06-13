package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares an integer range constraint for an integer field component.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface IntRange {

    /**
     * Minimum value, inclusive. Defaults to the full integer lower bound.
     *
     * @return minimum value
     */
    int min() default Integer.MIN_VALUE;

    /**
     * Maximum value, inclusive. Defaults to the full integer upper bound.
     *
     * @return maximum value
     */
    int max() default Integer.MAX_VALUE;
}
