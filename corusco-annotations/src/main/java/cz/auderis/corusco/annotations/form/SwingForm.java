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
