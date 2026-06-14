package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a regular-expression constraint for a generated string text field.
 *
 * <p>Use this annotation on a {@link TextField} record component whose value
 * type is {@link String}. The processor emits constraint metadata and generated
 * validation that requires non-empty input to match the supplied pattern.
 * Combine it with {@link Required} when empty input should also be rejected.</p>
 *
 * <p>The processor validates that the component is a string text field and that
 * the pattern text is not blank. Pattern syntax is preserved as descriptor
 * metadata and later used by generated validation; the annotation itself does
 * not execute matching at runtime.</p>
 *
 * <p>Applying this annotation to combo boxes, checkboxes, dates, numeric
 * fields, or non-form components is invalid or meaningless because those field
 * kinds do not expose string-pattern validation.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface Regex {

    /**
     * Regular expression pattern.
     *
     * @return pattern text
     */
    String value();
}
