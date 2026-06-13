package cz.auderis.corusco.core.table;

import cz.auderis.corusco.core.key.ResourceKey;
import java.util.Objects;

/**
 * Immutable metadata for a table column.
 *
 * <p>The descriptor names stable identities, resource keys, defaults, and
 * capabilities. It does not know how to read or update row values; use
 * {@link Column} for executable row access.</p>
 *
 * @param key typed column key
 * @param headerKey header text resource key
 * @param tooltipKey optional header/cell tooltip resource key
 * @param defaults default presentation state
 * @param capabilities declared column capabilities
 * @param <R> row type
 * @param <V> cell value type
 */
public record ColumnDescriptor<R, V>(
        ColumnKey<R, V> key,
        ResourceKey<String> headerKey,
        ResourceKey<String> tooltipKey,
        ColumnDefaults defaults,
        ColumnCapabilities capabilities
) {

    /**
     * Creates column metadata.
     *
     * @param key typed column key
     * @param headerKey header text resource key
     * @param tooltipKey optional tooltip resource key
     * @param defaults default presentation state
     * @param capabilities declared capabilities
     */
    public ColumnDescriptor {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(headerKey, "headerKey");
        Objects.requireNonNull(defaults, "defaults");
        Objects.requireNonNull(capabilities, "capabilities");
    }
}
