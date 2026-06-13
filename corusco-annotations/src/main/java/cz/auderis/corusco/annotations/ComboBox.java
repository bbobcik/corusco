package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record component as a generated combo-box field.
 *
 * <p>This stage generates typed key and descriptor metadata only. Option-source
 * and renderer metadata are deferred until generated view/model stages.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface ComboBox {
}
