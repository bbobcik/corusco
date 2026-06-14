package cz.auderis.corusco.core.key;

import java.util.Objects;

/**
 * Typed identity for a resource value.
 *
 * <p>The stable id is used at resource boundaries such as generated resource
 * descriptors or bundle lookups. Equality and hash code include both id and
 * resource value type so generated keys remain type-aware.</p>
 *
 * <p>Generated form, table, help, and action metadata create
 * {@code ResourceKey<String>} constants for labels, headers, command text, and
 * tooltips. Form resources live in companions such as
 * {@code CustomerEditResources}, table resources in companions such as
 * {@code CustomerRowTableResources}, and action resources in companions such as
 * {@code CustomerPresenterActions}.
 * Use {@link #of(String, Class)} for handwritten resource metadata and tests.</p>
 *
 * @param id stable non-blank resource id
 * @param valueType resource value type
 * @param <T> resource value type
 */
public record ResourceKey<T>(String id, Class<T> valueType) {

    /**
     * Creates a resource key.
     *
     * @param id stable non-blank resource id
     * @param valueType resource value type
     */
    public ResourceKey {
        id = KeyIds.requireId(id);
        Objects.requireNonNull(valueType, "valueType");
    }

    /**
     * Creates a resource key for hand-written tests and generated-style code.
     *
     * @param id stable non-blank resource id
     * @param valueType resource value type
     * @param <T> resource value type
     * @return resource key
     */
    public static <T> ResourceKey<T> of(String id, Class<T> valueType) {
        return new ResourceKey<>(id, valueType);
    }

    @Override
    public String toString() {
        return "ResourceKey[" + id + ":" + valueType.getSimpleName() + "]";
    }
}
