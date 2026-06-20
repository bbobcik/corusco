package cz.auderis.corusco.core.collection;

import java.util.List;
import java.util.Objects;

/**
 * Ordered group of list changes delivered to listeners.
 *
 * <p>Single mutations produce a set with one change. Batched mutations preserve
 * the exact order of inner changes and deliver them as one set after the outer
 * batch completes.</p>
 *
 * @param changes immutable changes in delivery order
 * @param <E> element type
 */
public record ListChangeSet<E>(List<ListChange<E>> changes) {

    /**
     * Creates a change set.
     *
     * @param changes changes in order
     */
    public ListChangeSet {
        Objects.requireNonNull(changes, "changes");
        if (changes.isEmpty()) {
            throw new IllegalArgumentException("changes must not be empty");
        }
        changes = List.copyOf(changes);
    }

    /**
     * Creates a one-change set.
     *
     * @param change change
     * @param <E> element type
     * @return change set
     */
    public static <E> ListChangeSet<E> of(ListChange<E> change) {
        Objects.requireNonNull(change, "change");
        return new ListChangeSet<>(List.of(change));
    }
}
