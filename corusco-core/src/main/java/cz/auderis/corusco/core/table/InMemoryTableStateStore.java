package cz.auderis.corusco.core.table;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Volatile table state store backed by an in-memory map.
 *
 * <p>This implementation is useful for tests, examples, and application
 * sessions that already persist state elsewhere. It is not synchronized;
 * callers should confine an instance to one thread or provide external
 * synchronization when sharing it.</p>
 */
public final class InMemoryTableStateStore implements TableStateStore {

    private final Map<String, TableState> states = new LinkedHashMap<>();

    @Override
    public Optional<TableState> load(String tableId) {
        return Optional.ofNullable(states.get(TableIds.requireId(tableId)));
    }

    @Override
    public void save(TableState state) {
        states.put(state.tableId(), state);
    }

    @Override
    public void remove(String tableId) {
        states.remove(TableIds.requireId(tableId));
    }

    @Override
    public void flush() {
        // Volatile storage has no pending durable writes to push.
    }
}
