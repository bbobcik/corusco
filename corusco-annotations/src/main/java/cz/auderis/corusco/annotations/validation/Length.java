package cz.auderis.corusco.annotations.validation;

import cz.auderis.corusco.annotations.form.TextField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a string length constraint for a generated text field component.
 *
 * <p>Place this annotation on a record component that is also a
 * {@link TextField}. The processor emits length metadata and generated
 * validation for {@link String} values. Combine it with {@link Required} when
 * blank or missing text should be invalid; length validation by itself
 * describes accepted bounds for present string values.</p>
 *
 * <p>The processor validates that the component is a string text field and that
 * {@code min >= 0} and {@code min <= max}. Applying it to non-string, numeric,
 * date, checkbox, combo-box, or non-form components is invalid or meaningless.</p>
 *
 * <p>Generated descriptors expose the length bounds as validation metadata, so
 * Swing bindings, tests, and generated documentation can describe the
 * constraint without inspecting source annotations. The generated metadata uses
 * {@code cz.auderis.corusco.core.meta.ConstraintDescriptor}; the generated
 * problem identity is exposed in problem-code companions such as
 * {@code CustomerEditProblems}.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD })
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
