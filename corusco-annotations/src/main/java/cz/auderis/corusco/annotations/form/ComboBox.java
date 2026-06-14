package cz.auderis.corusco.annotations.form;

import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.validation.Length;
import cz.auderis.corusco.annotations.validation.Regex;
import cz.auderis.corusco.annotations.validation.Required;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record component as a generated combo-box field.
 *
 * <p>Use this annotation on a component of a {@link SwingForm} record when the
 * value should be edited through a selection control. The processor emits typed
 * field keys, descriptor metadata, and a Swing {@code JComboBox<Value>} entry
 * in generated view plans. The component type becomes the value type used by
 * generated field models and bindings.</p>
 *
 * <p>The annotation identifies the editor family only. It does not declare the
 * available options, renderer, or loading policy; those remain view/presenter
 * responsibilities. {@link Required} and {@link Help} are meaningful
 * combinations. Text-only constraints such as {@link Length} or {@link Regex}
 * are not meaningful for combo boxes and should not be used.</p>
 *
 * <p>Generated descriptors keep the combo-box field tied to stable field and
 * resource ids from the enclosing form. Runtime code should consume that
 * generated metadata rather than scanning this annotation reflectively.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface ComboBox {
}
