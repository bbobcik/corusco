package cz.auderis.corusco.core.table;

import java.util.Objects;

/**
 * Persisted sort state for one table column.
 *
 * <p>A table state contains one sort state per sorted column. The column id is
 * the stable descriptor id used by persistence stores, {@link SortDirection}
 * records the ordering, and priority defines multi-column sort order from first
 * key to last. Instances are immutable value objects and validate ids eagerly
 * through {@link TableIds}.</p>
 *
 * @param columnId stable column persistence id
 * @param direction sort direction
 * @param priority zero-based sort priority
 */
public record SortState(String columnId, SortDirection direction, int priority) {

    /**
     * Creates sort state.
     *
     * @param columnId stable column persistence id
     * @param direction sort direction
     * @param priority zero-based sort priority
     */
    public SortState {
        columnId = TableIds.requireId(columnId);
        Objects.requireNonNull(direction, "direction");
        if (priority < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
    }
}
