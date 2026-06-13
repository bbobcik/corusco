package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a string length constraint for a text field component.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface Length {

    /**
     * Minimum length, inclusive.
     *
     * @return minimum length
     */
    int min() default 0;

    /**
     * Maximum length, inclusive.
     *
     * @return maximum length
     */
    int max();
}
