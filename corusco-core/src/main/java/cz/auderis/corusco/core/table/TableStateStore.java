package cz.auderis.corusco.core.table;

import java.util.Optional;

/**
 * Persistence boundary for immutable table state snapshots.
 *
 * <p>Stores are Swing-free and work only with stable table ids plus immutable
 * {@link TableState} values. Implementations may be volatile, durable, or
 * application-specific. Callers that need durability should invoke
 * {@link #flush()} at lifecycle boundaries after saves or removals.</p>
 */
public interface TableStateStore {

    /**
     * Loads a stored state snapshot.
     *
     * @param tableId stable table id
     * @return stored state, or empty when no state exists for the table
     * @throws TableStateStoreException if the backing store cannot be read or
     *         contains malformed state
     */
    Optional<TableState> load(String tableId);

    /**
     * Saves a table state snapshot.
     *
     * <p>Implementations must not retain mutable caller-owned data structures.
     * The current {@link TableState} record already copies its component lists,
     * so storing the value itself is sufficient for in-memory stores.</p>
     *
     * @param state state to save
     * @throws TableStateStoreException if the backing store cannot be written
     */
    void save(TableState state);

    /**
     * Removes state for one table id.
     *
     * @param tableId stable table id
     * @throws TableStateStoreException if the backing store cannot be updated
     */
    void remove(String tableId);

    /**
     * Flushes pending durable writes.
     *
     * <p>Volatile stores may implement this as a no-op. Durable stores should
     * push buffered writes to their backing storage when the backing API
     * exposes such an operation.</p>
     *
     * @throws TableStateStoreException if pending writes cannot be flushed
     */
    void flush();
}
