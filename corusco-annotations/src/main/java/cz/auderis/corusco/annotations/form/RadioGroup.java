package cz.auderis.corusco.annotations.form;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a form source member as a generated radio-group field.
 *
 * <p>Radio groups are single-selection option editors. They use the same typed
 * field model and option descriptor metadata as combo boxes, but communicate a
 * different editor family to generated view plans and binding extensions.</p>
 *
 * <p>The first processor implementation supports enum-valued radio groups.
 * Enum constants may use {@link CoruscoForm.Option} to define stable option keys
 * and ordering. Display labels, descriptions, and help remain generated
 * resource metadata rather than values embedded in the enum.</p>
 *
 * @see ComboBox
 * @see CoruscoForm.Option
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD })
public @interface RadioGroup {

    /**
     * Whether enum-valued radio groups should expose generated option
     * descriptors.
     *
     * @return enum option metadata flag
     */
    boolean enumOptions() default true;
}
