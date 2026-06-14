package cz.auderis.corusco.annotations.form;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record as a Swing form source type.
 *
 * <p>Apply this annotation to a record whose components describe editable form
 * input. The processor inspects field annotations on the record components and
 * generates typed field keys, field descriptors, form-model helpers,
 * validation metadata, resource keys, and view-plan metadata for Swing
 * bindings. The generated form works with the core form model APIs rather than
 * with reflection over the record at runtime.</p>
 *
 * <p>The annotation is source-retained because Corusco processors use
 * {@code javax.lang.model} during compilation. The {@link #id()} value becomes
 * the stable prefix for generated field key ids, for example
 * {@code customer/credit-limit}. The processor validates that the annotation is
 * used on a record and that the id is non-blank.</p>
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
 * extends {@code cz.auderis.corusco.core.form.AbstractFormModel}; a view
 * contract such as {@code CustomerEditView} represents Swing components; and
 * companions such as {@code CustomerEditBehaviorPlan} and
 * {@code CustomerEditBindings} install generated Swing behaviors.</p>
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
