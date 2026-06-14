package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.List;
import java.util.function.Consumer;

/**
 * Mutable indexed list that publishes structural changes to subscribers.
 *
 * <p>This is the core list contract used by table models, list adapters,
 * filtered views, and presenter-owned row caches. Callers can read current
 * contents, mutate the list through explicit methods, take immutable snapshots,
 * and subscribe to ordered {@link ListChangeSet} deliveries. Implementations
 * decide whether they own storage or adapt another source, and document that
 * ownership at the concrete type.</p>
 *
 * <p>Core implementations are not synchronized. Listener dispatch is
 * synchronous on the mutating thread and commonly uses a listener snapshot, so
 * removing a listener during dispatch affects future events but not the current
 * delivery. Swing code should either mutate the source list on the Event
 * Dispatch Thread or use a documented EDT adapter before connecting it to Swing
 * models.</p>
 *
 * <p>Implementors must keep read methods, snapshots, mutations, and emitted
 * changes consistent. A mutating method should either complete and publish the
 * corresponding change, or fail without reporting a misleading event.</p>
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
     * <p>Implementations should preserve the order of inner mutations and
     * deliver them after the outermost batch completes. Nested batches should
     * not publish partial intermediate change sets.</p>
     *
     * @param work mutation work
     */
    void batch(Consumer<ObservableList<E>> work);

    /**
     * Subscribes a listener.
     *
     * <p>The list retains the listener until the returned subscription is
     * closed. Closing the subscription should be idempotent.</p>
     *
     * @param listener listener
     * @return subscription removing listener when closed
     */
    Subscription subscribe(ListChangeListener<E> listener);
}
