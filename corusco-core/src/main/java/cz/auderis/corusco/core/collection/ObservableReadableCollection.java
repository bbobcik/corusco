package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Subscription;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.stream.Stream;

/**
 * Read-only observable ordered and indexed collection.
 *
 * <p>This contract is the common read surface for observable lists, sorted
 * sets, and other observable collections that have a stable presentation
 * order. It exposes the operations needed by read-only views: current size,
 * indexed reads, immediate stream traversal, immutable ordered snapshots, and
 * change subscription. Consumers that need indexed mutation, batching, or
 * list-owned write operations should depend on {@link ObservableList}
 * instead.</p>
 *
 * <p>Change events use {@link ListChangeSet} because Corusco presentation
 * collections expose a stable snapshot order. For non-list implementations,
 * indices in emitted changes are relative to that snapshot order.</p>
 *
 * @param <E> element type
 */
public interface ObservableReadableCollection<E extends @NonNull Object> {

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
     * Returns a sequential stream over current elements in collection order.
     *
     * <p>The returned stream is single-use like ordinary Java streams. It is
     * intended for immediate read-only traversal such as {@code anyMatch},
     * {@code findFirst}, {@code map}, or {@code count}. Implementations may
     * stream directly from their backing storage, so the stream is not an
     * immutable point-in-time snapshot. Mutating the source during traversal is
     * outside this contract and may fail according to the backing collection.</p>
     *
     * @return sequential stream over current elements
     */
    Stream<E> stream();

    /**
     * Returns a sequential stream over an immutable ordered snapshot.
     *
     * <p>Use this method, or {@link #snapshot()}, when traversal must be stable
     * even if the source collection changes after the stream is obtained.</p>
     *
     * @return sequential stream over a durable snapshot
     */
    default Stream<E> snapshotStream() {
        return snapshot().stream();
    }

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
