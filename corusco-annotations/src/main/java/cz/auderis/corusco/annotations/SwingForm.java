package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record as a Swing form source type.
 *
 * <p>The annotation is source-retained because Corusco processors use
 * {@code javax.lang.model} during compilation and do not inspect annotations at
 * runtime. The {@link #id()} value becomes the stable prefix for generated
 * field key ids, for example {@code customer/credit-limit}.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SwingForm {

    /**
     * Stable form id used as the prefix for generated metadata ids.
     *
     * @return non-blank form id
     */
    String id();
}
