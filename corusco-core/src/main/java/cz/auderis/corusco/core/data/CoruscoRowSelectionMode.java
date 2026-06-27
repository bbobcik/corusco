package cz.auderis.corusco.core.data;

/**
 * Scalable key-selection mode.
 *
 * <p>The mode determines how {@link CoruscoRowSelection} interprets its key
 * sets and query. It exists so callers can distinguish "no rows selected",
 * "these specific keys are selected", and "the whole query result is selected
 * except these keys" without relying on empty-set conventions.</p>
 */
public enum CoruscoRowSelectionMode {
    /**
     * No rows are selected.
     */
    NONE,
    /**
     * Selection is exactly the immutable included-key set.
     */
    EXPLICIT_KEYS,
    /**
     * Selection is every row matching the stored query except keys in the
     * excluded-key set.
     */
    ALL_MATCHING_QUERY
}
