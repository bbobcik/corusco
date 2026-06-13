package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a decimal range constraint for a text field component.
 *
 * <p>Bounds are strings so generated source can preserve exact decimal tokens
 * and later validator generation can decide the numeric representation.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface DecimalRange {

    /**
     * Minimum value, inclusive. Empty means unbounded.
     *
     * @return minimum value token
     */
    String min() default "";

    /**
     * Maximum value, inclusive. Empty means unbounded.
     *
     * @return maximum value token
     */
    String max() default "";
}
