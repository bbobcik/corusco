package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a generated field as required.
 *
 * <p>Use this annotation on a record component that also declares a Corusco
 * field kind such as {@link TextField}, {@link DateField}, {@link ComboBox}, or
 * {@link CheckBox}. The processor emits declarative constraint metadata and a
 * generated validator/problem code for the field. For text fields, required
 * means non-null and non-blank; for other field kinds, it means a non-null
 * semantic value where that distinction applies.</p>
 *
 * <p>The annotation does not make the Java record component non-null and does
 * not change constructor behavior. It describes generated form validation.
 * Applying it to a component that is not part of generated form metadata is
 * invalid or meaningless and should be reported by the processor where the
 * surrounding source is processed.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface Required {
}
