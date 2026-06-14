package cz.auderis.corusco.core.meta;

import cz.auderis.corusco.core.key.HelpTopic;
import cz.auderis.corusco.core.key.ResourceKey;
import java.util.List;
import java.util.Objects;

/**
 * Immutable generated metadata for one form field.
 *
 * <p>The descriptor keeps stable ids, resource keys, optional help topic, and
 * constraint declarations together. It deliberately does not perform resource
 * lookup or validation itself; generated form-model and view-plan stages consume
 * this metadata later.</p>
 *
 * <p>Generated {@code @SwingForm} records create {@code FieldDescriptor}
 * constants in {@code <Form>Descriptors} for each record component annotated
 * with {@code @TextField}, {@code @DateField}, {@code @CheckBox}, or
 * {@code @ComboBox}. Handwritten forms may construct descriptors directly, but
 * generated forms should use the generated constants so ids, resource keys,
 * help topics, and constraints remain consistent.</p>
 *
 * @param id stable field id
 * @param componentName source record component name
 * @param kind presentation kind
 * @param valueType field value type
 * @param labelKey label resource key
 * @param tooltipKey optional tooltip resource key
 * @param helpTopic optional help topic
 * @param constraints immutable constraints
 * @param <O> owner type
 * @param <T> field value type
 */
public record FieldDescriptor<O, T>(
        String id,
        String componentName,
        FieldKind kind,
        Class<T> valueType,
        ResourceKey<String> labelKey,
        ResourceKey<String> tooltipKey,
        HelpTopic helpTopic,
        List<ConstraintDescriptor> constraints
) {

    /**
     * Creates field metadata.
     *
     * @param id stable field id
     * @param componentName source record component name
     * @param kind presentation kind
     * @param valueType field value type
     * @param labelKey label resource key
     * @param tooltipKey optional tooltip resource key
     * @param helpTopic optional help topic
     * @param constraints constraints
     */
    public FieldDescriptor {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(componentName, "componentName");
        if (componentName.isBlank()) {
            throw new IllegalArgumentException("componentName must not be blank");
        }
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(valueType, "valueType");
        Objects.requireNonNull(labelKey, "labelKey");
        constraints = List.copyOf(Objects.requireNonNull(constraints, "constraints"));
    }
}
