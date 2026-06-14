package cz.auderis.corusco.core.collection;

/**
 * Callback notified when an {@link ObservableList} publishes structural
 * changes.
 *
 * <p>Implementations receive immutable change sets in the order reported by
 * the list. Observable lists in the core module deliver callbacks
 * synchronously from the mutating call unless a specific implementation
 * documents another policy. Listeners should therefore return quickly and avoid
 * reentrant list mutation unless the observed list explicitly permits it.</p>
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
