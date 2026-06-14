package cz.auderis.corusco.annotations.form;

import cz.auderis.corusco.annotations.validation.DecimalRange;
import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.validation.IntRange;
import cz.auderis.corusco.annotations.validation.Length;
import cz.auderis.corusco.annotations.validation.Regex;
import cz.auderis.corusco.annotations.validation.Required;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record component as a generated text-entry field.
 *
 * <p>Use this annotation on a component of a {@link SwingForm} record when the
 * value is edited through text. The processor accepts supported scalar types
 * and emits typed field keys, descriptor metadata, a generated field backed by
 * {@code TextFieldModel}, converter wiring, and a Swing text component entry in
 * generated view plans.</p>
 *
 * <p>Text fields are the field kind that can carry text-oriented validation
 * annotations such as {@link Length}, {@link Regex}, {@link IntRange}, and
 * {@link DecimalRange}. {@link Required} and {@link Help} may also be combined
 * with text fields. Applying text-only constraints to other field kinds is
 * invalid and is reported by the processor where validation is implemented.</p>
 *
 * <p>The annotation chooses the generated field/editor family only. It does not
 * create a Swing component at runtime and does not define resource text by
 * itself; generated descriptors and resources provide those details.</p>
 *
 * <p>The generated {@code <Form>Fields} companion exposes a
 * {@code cz.auderis.corusco.core.key.TextFieldKey} constant for each
 * {@code @TextField} component. The generated {@code <Form>Resources}
 * companion exposes matching
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} label and tooltip
 * keys when metadata requires them.</p>
 *
 * <p>The generated {@code <Form>FormModel} owns a
 * {@code cz.auderis.corusco.core.form.TextFieldModel} for each
 * {@code @TextField}. The generated descriptor is a
 * {@code cz.auderis.corusco.core.meta.FieldDescriptor} with text field kind and
 * any supported {@code ConstraintDescriptor} metadata.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface TextField {
}
