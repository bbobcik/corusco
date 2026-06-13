package cz.auderis.corusco.core.collection;

/**
 * Listener for observable-list changes.
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
