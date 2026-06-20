package cz.auderis.corusco.annotations.form;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record or abstract class as a Corusco form source type.
 *
 * <p>Apply this annotation to a record whose components describe editable form
 * input, or to an abstract class whose abstract accessor methods describe the
 * same values. The processor inspects field annotations on record components
 * or abstract accessors and generates typed field keys, field descriptors,
 * form-model helpers, validation metadata, and resource keys. Packages
 * annotated with {@code @SwingCompanionPackage} also receive Swing view and
 * binding companions. The generated form works with the core form model APIs
 * rather than with reflection over the source type at runtime.</p>
 *
 * <p>The annotation is retained in class files so adapter packages can request
 * Swing companions for already compiled form sources. Corusco processors still
 * read it through {@code javax.lang.model} during compilation; runtime code
 * should use generated descriptors instead of reflective annotation scanning.
 * The {@link #id()} value becomes the stable prefix for generated field key
 * ids, for example {@code customer/credit-limit}. The processor validates that
 * the annotation is used on a supported source type and that the id is
 * non-blank.</p>
 *
 * <p>Changing the id changes generated metadata and resource keys. Treat it as
 * an application compatibility change, especially when generated descriptors
 * are referenced by tests, resources, or persisted UI state.</p>
 *
 * <p>Generated form companions are the normal place to obtain runtime key
 * instances. A fields companion such as {@code CustomerEditFields} contains
 * {@code cz.auderis.corusco.core.key.TextFieldKey} constants for
 * {@link TextField}/{@link DateField} components and
 * {@code cz.auderis.corusco.core.key.FieldKey} constants for
 * {@link CheckBox}/{@link ComboBox} components. A resources companion such as
 * {@code CustomerEditResources} contains
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} constants
 * for generated labels and field tooltips.</p>
 *
 * <p>The same source also generates non-key runtime objects. A descriptor
 * companion such as {@code CustomerEditDescriptors} contains
 * {@code cz.auderis.corusco.core.meta.FieldDescriptor} constants. A problem
 * companion such as {@code CustomerEditProblems} contains
 * {@code cz.auderis.corusco.core.problem.ProblemCode} constants for supported
 * validation annotations. A form model such as {@code CustomerEditFormModel}
 * extends {@code cz.auderis.corusco.core.form.AbstractFormModel}. When the
 * owning or adapter package is annotated with {@code @SwingCompanionPackage},
 * a view contract such as {@code CustomerEditView} represents Swing components
 * and companions such as {@code CustomerEditBehaviorPlan} and
 * {@code CustomerEditBindings} install generated Swing behaviors.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface CoruscoForm {

    /**
     * Stable form id used as the prefix for generated metadata ids.
     *
     * @return non-blank form id
     */
    String id();

    /**
     * Supplies stable option identity metadata for enum constants used by a
     * generated form option editor.
     *
     * <p>The annotation deliberately avoids labels and descriptions. Generated
     * option descriptors derive resource keys from the owning form field and
     * this stable option key so presentation text remains resource-backed.</p>
     */
    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.FIELD)
    @interface Option {

        /**
         * Stable option key. If blank, the processor derives a key from the
         * enum constant name.
         *
         * @return stable option key
         */
        String key() default "";

        /**
         * Presentation order. Negative values keep declaration order.
         *
         * @return option order
         */
        int order() default -1;
    }

    /**
     * Requests generated component-state metadata for a form member.
     *
     * <p>On a value field, this annotation asks the processor to generate a
     * sibling component-state model, for example {@code passwordState}. On an
     * abstract form method returning {@code ComponentStateModel}, it declares an
     * auxiliary non-value state member such as a tab, section, or button row.</p>
     */
    @Retention(RetentionPolicy.CLASS)
    @Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD })
    @interface ComponentState {
    }

    /**
     * Presentation effect applied when a dependency condition is not satisfied.
     */
    enum DependencyEffect {

        /**
         * Disable the target component state when the condition is not
         * satisfied.
         */
        ENABLED,

        /**
         * Hide the target component state when the condition is not satisfied.
         */
        VISIBLE,

        /**
         * Mark the target component state as not relevant when the condition is
         * not satisfied.
         */
        RELEVANT
    }

    /**
     * Declares that a component state depends on another form field value.
     *
     * <p>The {@link #field()} value is the source form member name, not a
     * resource id. The annotation processor validates that the member exists in
     * the same form. {@link #values()} contains source-level value tokens. The
     * processor resolves them at compile time where possible: enum constants or
     * generated option keys become typed enum values, checkbox values must be
     * {@code true} or {@code false}, and text/date-text dependencies continue
     * to compare raw text.</p>
     */
    @Retention(RetentionPolicy.CLASS)
    @Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD })
    @Repeatable(DependsOn.List.class)
    @interface DependsOn {

        /**
         * Source field member name.
         *
         * @return source field name
         */
        String field();

        /**
         * Source value tokens that satisfy the condition.
         *
         * @return expected source value tokens
         */
        String[] values();

        /**
         * State effect to control from the dependency.
         *
         * @return dependency effect
         */
        DependencyEffect effect() default DependencyEffect.ENABLED;

        /**
         * Container for repeated dependency declarations.
         */
        @Retention(RetentionPolicy.CLASS)
        @Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD })
        @interface List {

            /**
             * Repeated dependency declarations.
             *
             * @return dependency declarations
             */
            DependsOn[] value();
        }
    }
}
