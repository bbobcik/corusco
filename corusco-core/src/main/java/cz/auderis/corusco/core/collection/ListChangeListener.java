package cz.auderis.corusco.core.collection;

/**
 * Callback notified when an {@link ObservableReadableCollection} publishes
 * structural changes.
 *
 * <p>Implementations receive immutable change sets in the order reported by
 * the observable source. Core mutable collection implementations deliver
 * callbacks synchronously from the mutating call unless a specific
 * implementation documents another policy. Listeners should therefore return
 * quickly and avoid reentrant mutation unless the observed source explicitly
 * permits it.</p>
 *
 * @param <E> element type
 */
@FunctionalInterface
public interface ListChangeListener<E> {

    /**
     * Receives a list change set.
     *
     * @param changes ordered changes
     */
    void listChanged(ListChangeSet<E> changes);
}
