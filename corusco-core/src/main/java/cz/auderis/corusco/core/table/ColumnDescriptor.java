package cz.auderis.corusco.core.table;

import cz.auderis.corusco.core.key.HelpTopic;
import cz.auderis.corusco.core.key.ResourceKey;
import java.util.Objects;

/**
 * Immutable metadata for a table column.
 *
 * <p>The descriptor names stable identities, resource keys, defaults, and
 * capabilities. It does not know how to read or update row values; use
 * {@link Column} for executable row access.</p>
 *
 * <p>Generated {@code @SwingTable} records create {@code ColumnDescriptor}
 * constants in {@code <Row>Columns} from {@code @Column} and {@code @Help}
 * metadata. Header and tooltip keys come from {@code <Row>TableResources};
 * defaults, persistence metadata, and capabilities come from annotation
 * members.</p>
 *
 * @param key typed column key
 * @param headerKey header text resource key
 * @param tooltipKey optional header/cell tooltip resource key
 * @param helpTopic optional column help topic
 * @param persistence persistence metadata
 * @param defaults default presentation state
 * @param capabilities declared column capabilities
 * @param <R> row type
 * @param <V> cell value type
 */
public record ColumnDescriptor<R, V>(
        ColumnKey<R, V> key,
        ResourceKey<String> headerKey,
        ResourceKey<String> tooltipKey,
        HelpTopic helpTopic,
        ColumnPersistence persistence,
        ColumnDefaults defaults,
        ColumnCapabilities capabilities
) {

    /**
     * Creates column metadata without help-topic metadata.
     *
     * @param key typed column key
     * @param headerKey header text resource key
     * @param tooltipKey optional tooltip resource key
     * @param defaults default presentation state
     * @param capabilities declared capabilities
     */
    public ColumnDescriptor(
            ColumnKey<R, V> key,
            ResourceKey<String> headerKey,
            ResourceKey<String> tooltipKey,
            ColumnDefaults defaults,
            ColumnCapabilities capabilities
    ) {
        this(key, headerKey, tooltipKey, null, defaultPersistence(key, defaults), defaults, capabilities);
    }

    /**
     * Creates column metadata with help-topic metadata and default persistence
     * metadata derived from the column key and default width.
     *
     * @param key typed column key
     * @param headerKey header text resource key
     * @param tooltipKey optional tooltip resource key
     * @param helpTopic optional help topic
     * @param defaults default presentation state
     * @param capabilities declared capabilities
     */
    public ColumnDescriptor(
            ColumnKey<R, V> key,
            ResourceKey<String> headerKey,
            ResourceKey<String> tooltipKey,
            HelpTopic helpTopic,
            ColumnDefaults defaults,
            ColumnCapabilities capabilities
    ) {
        this(key, headerKey, tooltipKey, helpTopic, defaultPersistence(key, defaults), defaults, capabilities);
    }

    /**
     * Creates column metadata.
     *
     * @param key typed column key
     * @param headerKey header text resource key
     * @param tooltipKey optional tooltip resource key
     * @param helpTopic optional help topic
     * @param persistence persistence metadata
     * @param defaults default presentation state
     * @param capabilities declared capabilities
     */
    public ColumnDescriptor {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(headerKey, "headerKey");
        Objects.requireNonNull(persistence, "persistence");
        Objects.requireNonNull(defaults, "defaults");
        Objects.requireNonNull(capabilities, "capabilities");
    }

    private static <R, V> ColumnPersistence defaultPersistence(ColumnKey<R, V> key, ColumnDefaults defaults) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(defaults, "defaults");
        return ColumnPersistence.of(key.id(), defaults.width(), Integer.MAX_VALUE);
    }
}
