package cz.auderis.corusco.annotations.dataset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record component as a quality column.
 *
 * <p>A quality column stores validity, provenance, confidence, or other
 * quality state separately from the value columns it describes. The first
 * processor stage records the column role and the optional references in
 * generated metadata; it does not interpret the referenced names yet.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.RECORD_COMPONENT)
public @interface QualityColumn {

    /**
     * Stable column id or blank for the default.
     *
     * @return stable id or blank
     */
    String id() default "";

    /**
     * Source component names or column ids this quality column describes.
     *
     * @return referenced components or columns
     */
    String[] appliesTo() default {};
}
