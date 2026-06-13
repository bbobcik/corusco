package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a boolean record component as a generated checkbox field.
 *
 * <p>The first processor slice generates a {@code FieldKey<Owner, Boolean>}
 * constant for annotated components. The processor rejects non-boolean checkbox
 * components so generated code remains type-correct.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface CheckBox {
}
