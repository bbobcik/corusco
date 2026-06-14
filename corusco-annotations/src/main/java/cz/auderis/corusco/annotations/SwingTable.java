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
 * the stable prefix for generated table and column ids. Components annotated
 * with {@link Column} inside the record become generated column descriptors;
 * unannotated components are ignored by table metadata generation.</p>
 *
 * <p>The processor validates that the annotation is used on records and that
 * the id is non-blank. Generated artifacts use the source type name and stable
 * ids rather than runtime reflection, so changing the id is a compatibility
 * change for saved table state.</p>
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
