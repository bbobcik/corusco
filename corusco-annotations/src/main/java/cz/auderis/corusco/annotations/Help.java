package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares optional help metadata for a generated field descriptor.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface Help {

    /**
     * Stable tooltip resource id. Empty means use the default generated tooltip
     * id when a descriptor needs tooltip metadata.
     *
     * @return tooltip resource id
     */
    String tooltip() default "";

    /**
     * Stable help topic id. Empty means no help topic.
     *
     * @return help topic id
     */
    String topic() default "";
}
