package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Ordered group of list changes delivered to listeners.
 *
 * <p>Single mutations produce a set with one change. Batched mutations preserve
 * the exact order of inner changes and deliver them as one set after the outer
 * batch completes.</p>
 *
 * @param changes immutable changes in delivery order
 * @param origin origin supplied by the mutating code
 * @param <E> element type
 */
public record ListChangeSet<E extends @NonNull Object>(List<ListChange<E>> changes, ChangeOrigin origin) {

    /**
     * Creates a change set.
     *
     * @param changes changes in order
     * @param origin change origin
     */
    public ListChangeSet {
        Objects.requireNonNull(changes, "changes");
        Objects.requireNonNull(origin, "origin");
        if (changes.isEmpty()) {
            throw new IllegalArgumentException("changes must not be empty");
        }
        changes = List.copyOf(changes);
    }

    /**
     * Creates a model-originated change set.
     *
     * @param changes changes in order
     */
    public ListChangeSet(List<ListChange<E>> changes) {
        this(changes, StandardChangeOrigin.MODEL);
    }

    /**
     * Creates a one-change set.
     *
     * @param change change
     * @param <E> element type
     * @return change set
     */
    public static <E extends @NonNull Object> ListChangeSet<E> of(ListChange<E> change) {
        return of(change, StandardChangeOrigin.MODEL);
    }

    /**
     * Creates a one-change set.
     *
     * @param change change
     * @param origin change origin
     * @param <E> element type
     * @return change set
     */
    public static <E extends @NonNull Object> ListChangeSet<E> of(ListChange<E> change, ChangeOrigin origin) {
        Objects.requireNonNull(change, "change");
        return new ListChangeSet<>(List.of(change), origin);
    }
}
