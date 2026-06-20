package cz.auderis.corusco.annotations.validation;

import cz.auderis.corusco.annotations.form.TextField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a decimal range constraint for a generated decimal text field.
 *
 * <p>Use this annotation on a {@link TextField} record component whose value
 * type is {@link java.math.BigDecimal}. The processor emits range constraint
 * metadata and generated validation that compares semantic decimal values after
 * text conversion succeeds. Combine it with {@link Required} when a missing
 * value should be invalid.</p>
 *
 * <p>Bounds are strings so generated source can preserve exact decimal tokens
 * in descriptor metadata. The processor validates that at least one bound is
 * present, each non-blank bound parses as a decimal number, {@code min <= max}
 * when both are present, and the annotated component is a decimal text field.</p>
 *
 * <p>Applying decimal range metadata to integer, string, date, checkbox, or
 * combo-box fields is invalid or meaningless. Use {@link IntRange} for integer
 * text fields. Generated descriptors expose this rule as
 * {@code cz.auderis.corusco.core.meta.ConstraintDescriptor} metadata and
 * generated form models wire the matching validator.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD })
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
