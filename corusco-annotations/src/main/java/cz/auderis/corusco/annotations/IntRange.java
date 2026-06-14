package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares an integer range constraint for a generated integer text field.
 *
 * <p>Place this annotation on a record component that is also a
 * {@link TextField}. The processor emits range metadata and generated
 * validation for {@code int} and {@link Integer} values after text conversion
 * succeeds. Combine it with {@link Required} when absence should be invalid;
 * otherwise a missing optional value is not a range violation.</p>
 *
 * <p>The processor validates that the component is an integer text field and
 * that {@code min <= max}. Applying this annotation to decimal, string, date,
 * checkbox, combo-box, or non-form components is invalid or meaningless. Use
 * {@link DecimalRange} for decimal text fields.</p>
 *
 * <p>Generated descriptors expose the range as validation metadata, so tools
 * and tests can inspect the constraint without re-reading source annotations.</p>
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
