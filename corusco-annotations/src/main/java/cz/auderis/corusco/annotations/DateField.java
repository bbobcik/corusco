package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link java.time.LocalDate} record component as a generated date
 * field.
 *
 * <p>Use this annotation on a component of a {@link SwingForm} record when the
 * value should be edited as a date. Date fields use text-field style raw-text
 * editing with a generated {@code LocalDate} converter, so invalid intermediate
 * text can be preserved by the form model without replacing the last valid date
 * value.</p>
 *
 * <p>{@link Required} and {@link Help} are meaningful combinations. Text-only
 * or numeric constraints are not meaningful for date fields unless the
 * processor explicitly supports a date-specific rule. The processor rejects
 * non-{@code LocalDate} components for this field kind.</p>
 *
 * <p>The annotation chooses generated metadata and field-model wiring only. It
 * does not specify a date picker widget or locale policy; Swing view code and
 * converters own those presentation decisions.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface DateField {
}
