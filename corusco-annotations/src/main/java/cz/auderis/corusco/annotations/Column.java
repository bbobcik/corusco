package cz.auderis.corusco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record component as a generated table column.
 *
 * <p>Generated column ids, resource keys, defaults, and capabilities are
 * derived from this source-retained annotation. This processor slice supports
 * read-only columns; editable generated updaters are deliberately deferred.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.RECORD_COMPONENT)
public @interface Column {

    /**
     * Stable column id. When blank, the component name is converted to kebab
     * case and appended to the enclosing {@link SwingTable#id()}.
     *
     * @return stable id or blank for the default
     */
    String id() default "";

    /**
     * Header resource id. When blank, {@code column-id + "/header"} is used.
     *
     * @return resource id or blank for the default
     */
    String header() default "";

    /**
     * Tooltip resource id.
     *
     * @return resource id or blank when no tooltip key should be generated
     */
    String tooltip() default "";

    /**
     * Preferred default width in pixels.
     *
     * @return width greater than zero
     */
    int width() default 120;

    /**
     * Default visual order. Negative values mean record component order.
     *
     * @return visual order or negative for the component order
     */
    int order() default -1;

    /**
     * Initial visibility.
     *
     * @return true when visible by default
     */
    boolean visible() default true;

    /**
     * Sort capability.
     *
     * @return true when sortable
     */
    boolean sortable() default true;

    /**
     * Filter capability.
     *
     * @return true when filterable
     */
    boolean filterable() default true;

    /**
     * Hide capability.
     *
     * @return true when hideable
     */
    boolean hideable() default true;

    /**
     * Edit capability. Editable generated row updaters are deferred in the
     * first table-processor slice, so setting this to true is rejected for now.
     *
     * @return true when editable
     */
    boolean editable() default false;
}
