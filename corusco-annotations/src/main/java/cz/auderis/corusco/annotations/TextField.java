package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record component as a generated text field.
 *
 * <p>For the initial processor spike this produces a
 * {@code TextFieldKey<Owner, Value>} constant in the generated
 * {@code <Owner>Fields} class. Conversion and validation metadata are generated
 * by later stages.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface TextField {
}
