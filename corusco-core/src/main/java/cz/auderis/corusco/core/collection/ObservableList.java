package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.List;
import java.util.function.Consumer;

/**
 * Synchronously observable mutable list.
 *
 * <p>Implementations are not synchronized. Listener dispatch is synchronous on
 * the mutating thread and uses a listener snapshot, so removing a listener
 * during dispatch affects future events but not the current delivery.</p>
 *
 * @param <E> element type
 */
public interface ObservableList<E> {

    /**
     * Returns current size.
     *
     * @return element count
     */
    int size();

    /**
     * Returns element at index.
     *
     * @param index index
     * @return element
     */
    E get(int index);

    /**
     * Returns immutable snapshot of current elements.
     *
     * @return snapshot
     */
    List<E> snapshot();

    /**
     * Appends an element.
     *
     * @param element element
     */
    void add(E element);

    /**
     * Inserts an element.
     *
     * @param index insertion index
     * @param element element
     */
    void add(int index, E element);

    /**
     * Replaces an element.
     *
     * @param index index
     * @param element new element
     * @return previous element
     */
    E set(int index, E element);

    /**
     * Removes an element.
     *
     * @param index index
     * @return removed element
     */
    E remove(int index);

    /**
     * Moves an element within the list.
     *
     * @param fromIndex original index
     * @param toIndex target index after removal
     */
    void move(int fromIndex, int toIndex);

    /**
     * Clears the list.
     */
    void clear();

    /**
     * Runs mutations as one delivered change set.
     *
     * @param work mutation work
     */
    void batch(Consumer<ObservableList<E>> work);

    /**
     * Subscribes a listener.
     *
     * @param listener listener
     * @return subscription removing listener when closed
     */
    Subscription subscribe(ListChangeListener<E> listener);
}
