package cz.auderis.corusco.core.meta;

import cz.auderis.corusco.core.key.ResourceKey;
import java.util.Objects;

/**
 * Typed metadata for one allowable option value.
 *
 * @param value semantic option value
 * @param key stable option identity
 * @param labelKey label resource key
 * @param descriptionKey optional description resource key
 * @param helpKey optional help resource key
 * @param <T> semantic value type
 */
public record OptionDescriptor<T>(
        T value,
        OptionKey key,
        ResourceKey<String> labelKey,
        ResourceKey<String> descriptionKey,
        ResourceKey<String> helpKey
) {

    /**
     * Creates option metadata.
     *
     * @param value semantic option value
     * @param key stable option identity
     * @param labelKey label resource key
     * @param descriptionKey optional description resource key
     * @param helpKey optional help resource key
     */
    public OptionDescriptor {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(labelKey, "labelKey");
    }

    /**
     * Creates option metadata.
     *
     * @param value semantic option value
     * @param key stable option identity
     * @param labelKey label resource key
     * @param descriptionKey optional description resource key
     * @param helpKey optional help resource key
     * @param <T> semantic value type
     * @return option descriptor
     */
    public static <T> OptionDescriptor<T> of(
            T value,
            OptionKey key,
            ResourceKey<String> labelKey,
            ResourceKey<String> descriptionKey,
            ResourceKey<String> helpKey
    ) {
        return new OptionDescriptor<>(value, key, labelKey, descriptionKey, helpKey);
    }
}
