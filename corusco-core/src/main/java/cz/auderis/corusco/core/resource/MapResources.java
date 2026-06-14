package cz.auderis.corusco.core.resource;

import cz.auderis.corusco.core.key.ResourceKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable map-backed {@link Resources} implementation.
 *
 * <p>The input map is copied at construction time. Keys are stable resource
 * ids, not public property paths. Runtime values are checked against each
 * requested {@link ResourceKey#valueType()} before being returned. This class
 * is useful for examples, tests, and small applications that do not need a
 * localized or reloadable resource service.</p>
 */
public final class MapResources implements Resources {

    private static final MapResources EMPTY = new MapResources(Map.of());

    private final Map<String, Object> values;

    private MapResources(Map<String, ?> values) {
        this.values = Map.copyOf(values);
    }

    /**
     * Creates empty resources.
     *
     * @return empty resources
     */
    public static MapResources empty() {
        return EMPTY;
    }

    /**
     * Creates map-backed resources from stable ids to values.
     *
     * @param values values keyed by stable resource id
     * @return map-backed resources
     */
    public static MapResources of(Map<String, ?> values) {
        Objects.requireNonNull(values, "values");
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String id = Objects.requireNonNull(entry.getKey(), "resource id");
            copy.put(id, Objects.requireNonNull(entry.getValue(), () -> "resource value for " + id));
        }
        if (copy.isEmpty()) {
            return EMPTY;
        }
        return new MapResources(copy);
    }

    @Override
    public <T> Optional<T> find(ResourceKey<T> key) {
        Objects.requireNonNull(key, "key");
        Object value = values.get(key.id());
        if (value == null) {
            return Optional.empty();
        }
        if (!key.valueType().isInstance(value)) {
            throw new ResourceException(
                    "Resource " + key.id() + " has type " + value.getClass().getName()
                            + ", expected " + key.valueType().getName()
            );
        }
        return Optional.of(key.valueType().cast(value));
    }
}
