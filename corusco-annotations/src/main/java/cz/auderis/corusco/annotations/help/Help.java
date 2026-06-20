package cz.auderis.corusco.annotations.help;

import cz.auderis.corusco.annotations.table.Column;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares optional help metadata for generated field and table-column
 * descriptors.
 *
 * <p>Use this annotation on record components that also participate in Corusco
 * metadata generation, such as form fields or table columns. It does not affect
 * validation, conversion, editability, or editor selection. It only supplies
 * stable ids that generated descriptors can expose to tooltip, status-text, or
 * help-topic services.</p>
 *
 * <p>For generated form fields, an empty tooltip id allows generated metadata
 * to use the field's default tooltip resource id when a descriptor needs one.
 * For generated table columns, an empty tooltip id means no separate tooltip
 * unless {@link Column#tooltip()} declares one. An empty help topic means that
 * no F1/context help topic is available for the target.</p>
 *
 * <p>Help ids are part of the user-facing metadata contract. They may appear in
 * resource maps, generated descriptors, tests, and help-system routing. Treat
 * changes as compatibility changes rather than cosmetic refactoring.</p>
 *
 * <p>Generated form and table companions use tooltip ids to create
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} constants. Topic ids
 * are embedded in generated descriptors as
 * {@code cz.auderis.corusco.core.key.HelpTopic} values. Runtime code should
 * read the generated {@code cz.auderis.corusco.core.meta.FieldDescriptor} or
 * {@code cz.auderis.corusco.core.table.ColumnDescriptor} objects rather than
 * scanning this annotation.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD })
public @interface Help {

    /**
     * Stable tooltip resource id. For generated form fields, empty means use
     * the default generated tooltip id when a descriptor needs tooltip
     * metadata. For generated table columns, empty means no tooltip unless
     * {@link Column#tooltip()} declares one.
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
