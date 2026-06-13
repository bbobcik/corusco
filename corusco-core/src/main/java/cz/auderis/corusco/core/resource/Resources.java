package cz.auderis.corusco.core.resource;

import cz.auderis.corusco.core.key.ResourceKey;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Typed resource lookup boundary.
 *
 * <p>Generated descriptors expose stable {@link ResourceKey} instances; this
 * interface resolves those keys to runtime values. Implementations must return
 * values assignable to the key's declared value type. Missing values are
 * represented as {@link Optional#empty()} so tooltip/help composition can
 * decide its own fallback policy.</p>
 */
public interface Resources {

    /**
     * Finds a resource value.
     *
     * @param key resource key
     * @param <T> resource value type
     * @return resource value, or empty when absent
     * @throws ResourceException if a value exists but is not assignable to the
     *         key value type
     */
    <T> Optional<T> find(ResourceKey<T> key);

    /**
     * Requires a resource value.
     *
     * @param key resource key
     * @param <T> resource value type
     * @return resource value
     * @throws ResourceException when absent or wrong-typed
     */
    default <T> T require(ResourceKey<T> key) {
        return find(key).orElseThrow(() -> new ResourceException("Missing resource: " + key.id()));
    }

    /**
     * Resolves a resource value or returns a fallback when absent.
     *
     * @param key resource key
     * @param fallback fallback value
     * @param <T> resource value type
     * @return resource value or fallback
     * @throws ResourceException when a present value has the wrong type
     */
    default <T> T resolve(ResourceKey<T> key, T fallback) {
        Objects.requireNonNull(key, "key");
        return find(key).orElse(fallback);
    }

    /**
     * Creates empty resources.
     *
     * @return empty resources
     */
    static Resources empty() {
        return MapResources.empty();
    }

    /**
     * Creates map-backed resources.
     *
     * @param values values keyed by stable resource id
     * @return map-backed resources
     */
    static Resources of(Map<String, ?> values) {
        return MapResources.of(values);
    }
}
