package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Subscription;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

/**
 * Read-only observable ordered and indexed collection.
 *
 * <p>This contract is the common read surface for observable lists, sorted
 * sets, and other observable collections that have a stable presentation
 * order. It exposes the operations needed by read-only views: current size,
 * indexed reads, immutable ordered snapshots, and change subscription.
 * Consumers that need indexed mutation, batching, or list-owned write
 * operations should depend on {@link ObservableList} instead.</p>
 *
 * <p>Change events use {@link ListChangeSet} because Corusco presentation
 * collections expose a stable snapshot order. For non-list implementations,
 * indices in emitted changes are relative to that snapshot order.</p>
 *
 * @param <E> element type
 */
public interface ObservableReadableCollection<E> {

    /**
     * Returns current size.
     *
     * @return element count
     */
    int size();

    /**
     * Returns element at the supplied ordered snapshot index.
     *
     * @param index element index
     * @return element at index
     * @throws IndexOutOfBoundsException if index is outside the current
     *         collection bounds
     */
    E get(int index);

    /**
     * Returns immutable ordered snapshot of current elements.
     *
     * @return ordered snapshot
     */
    @UnmodifiableView
    List<E> snapshot();

    /**
     * Subscribes a listener.
     *
     * <p>The collection retains the listener until the returned subscription is
     * closed. Closing the subscription should be idempotent.</p>
     *
     * @param listener listener
     * @return subscription removing listener when closed
     */
    Subscription subscribe(ListChangeListener<E> listener);
}
