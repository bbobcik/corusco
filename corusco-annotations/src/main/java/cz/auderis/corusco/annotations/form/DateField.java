package cz.auderis.corusco.annotations.form;

import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.validation.Required;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link java.time.LocalDate} form source member as a generated date
 * field.
 *
 * <p>Use this annotation on a component of a {@link CoruscoForm} record or on an
 * abstract accessor method of a {@link CoruscoForm} abstract class when the value
 * should be edited as a date. Date fields use text-field style raw-text editing
 * with a generated {@code LocalDate} converter, so invalid intermediate text
 * can be preserved by the form model without replacing the last valid date
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
 *
 * <p>The generated fields companion, for example {@code CustomerEditFields}, exposes a
 * {@code cz.auderis.corusco.core.key.TextFieldKey} constant for each
 * {@code @DateField} component because date editing uses text-field raw-text
 * handling. Generated descriptors can use
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} and
 * {@code cz.auderis.corusco.core.key.HelpTopic} metadata supplied by
 * {@link Help}.</p>
 *
 * <p>The generated form model, for example {@code CustomerEditFormModel}, owns a
 * {@code cz.auderis.corusco.core.form.TextFieldModel} with a generated
 * {@code LocalDate} converter for each {@code @DateField}. The generated
 * descriptor is a {@code cz.auderis.corusco.core.meta.FieldDescriptor} with
 * date field kind.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD })
public @interface DateField {
}
