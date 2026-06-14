package cz.auderis.corusco.annotations.table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record component as a generated table column.
 *
 * <p>Use this annotation on components of a record annotated with
 * {@link SwingTable}. The processor turns each annotated component into a
 * generated column descriptor with a typed column key, header/tooltip resource
 * keys, default layout state, persistence id, capabilities, and row value
 * accessors. Unannotated record components are ignored by table metadata
 * generation.</p>
 *
 * <p>Editable columns use generated record-constructor updaters so table edits
 * replace immutable row records without reflection or JavaBeans property paths.
 * The annotation describes table presentation and editing metadata; it does not
 * store row data and does not create a Swing {@code JTable} by itself.</p>
 *
 * <p>The processor validates ids, width bounds, and the surrounding table
 * source. Stable ids and persistence ids can appear in generated code,
 * resources, diagnostics, and saved table preferences, so changing them is a
 * compatibility change for users with existing persisted layout state.</p>
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
     * Stable persistence id. When blank, the generated column id is used.
     *
     * @return stable persistence id or blank for the default
     */
    String persistenceId() default "";

    /**
     * Preferred default width in pixels.
     *
     * @return width greater than zero
     */
    int width() default 120;

    /**
     * Minimum restored width in pixels.
     *
     * @return width greater than zero and not greater than {@link #width()}
     */
    int minWidth() default 24;

    /**
     * Maximum restored width in pixels.
     *
     * @return width not less than {@link #width()}
     */
    int maxWidth() default Integer.MAX_VALUE;

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
     * Edit capability. For record rows, generated code creates an explicit
     * updater that calls the record constructor with the edited component value
     * and existing values from the remaining components.
     *
     * @return true when editable
     */
    boolean editable() default false;
}
