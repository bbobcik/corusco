package cz.auderis.corusco.core.data.edit;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Immutable ordered snapshot of staged edits.
 *
 * <p>An edit set is the value a presenter submits to persistence. It is a
 * snapshot, not a live view of a {@link CoruscoEditSession}. The change list
 * preserves the session's stable key order so tests, logs, and save payloads
 * remain deterministic.</p>
 *
 * <p>The record does not deduplicate changes itself. Sessions already keep at
 * most one change per key. Callers that manually construct edit sets are
 * responsible for choosing whether duplicate keys have meaning in their own
 * protocol.</p>
 *
 * @param changes changes in stable insertion order
 * @param <R> row type
 * @param <K> key type
 */
public record CoruscoEditSet<R extends @NonNull Object, K extends @NonNull Object>(
        List<CoruscoEditChange<R, K>> changes
) {

    /**
     * Creates an edit set.
     *
     * <p>The change list is copied immediately. Later mutation of the caller's
     * list cannot affect this snapshot.</p>
     *
     * @param changes changes
     */
    public CoruscoEditSet {
        changes = List.copyOf(Objects.requireNonNull(changes, "changes"));
    }

    /**
     * Empty edit set.
     *
     * <p>The returned value has no staged changes and is safe to reuse as an
     * initial observable edit-set value.</p>
     *
     * @param <R> row type
     * @param <K> key type
     * @return empty set
     */
    public static <R extends @NonNull Object, K extends @NonNull Object> CoruscoEditSet<R, K> empty() {
        return new CoruscoEditSet<>(List.of());
    }

    /**
     * Indicates whether there are staged changes.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return changes.isEmpty();
    }
}
