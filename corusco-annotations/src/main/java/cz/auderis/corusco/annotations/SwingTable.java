package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record as a Swing table row source type.
 *
 * <p>The annotation is source-retained because Corusco processors use
 * {@code javax.lang.model} during compilation. The {@link #id()} value becomes
 * the stable prefix for generated table and column ids.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SwingTable {

    /**
     * Stable table id used as the prefix for generated metadata ids.
     *
     * @return non-blank table id
     */
    String id();
}
