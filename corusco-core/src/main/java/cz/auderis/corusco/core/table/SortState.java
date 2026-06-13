package cz.auderis.corusco.core.table;

import java.util.Objects;

/**
 * Persisted sort state for one table column.
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
