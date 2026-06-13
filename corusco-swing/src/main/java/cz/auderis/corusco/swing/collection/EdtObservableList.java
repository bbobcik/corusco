package cz.auderis.corusco.swing.collection;

import cz.auderis.corusco.core.collection.ListChangeListener;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableList;
import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

/**
 * {@link ObservableList} wrapper that delivers listener callbacks on the EDT.
 *
 * <p>The wrapped list remains the storage and mutation owner. This adapter
 * delegates reads and mutations directly to the source list; it only changes
 * where wrapper subscribers receive source change sets. If the source fires on
 * the EDT, delivery is immediate. If the source fires on another thread,
 * delivery is queued with {@link SwingUtilities#invokeLater(Runnable)}.</p>
 *
 * <p>This class does not make the source list thread-safe. Applications that
 * mutate the source from background threads must still use a source
 * implementation and ownership policy that allow those mutations.</p>
 *
 * <p>Closing the wrapper removes the source subscription, clears wrapper
 * listeners, and suppresses queued callbacks that have not reached the EDT yet.
 * Closing does not close or clear the wrapped source list.</p>
 *
 * @param <E> element type
 */
public final class EdtObservableList<E> implements ObservableList<E>, Disposable {

    private final Object monitor = new Object();
    private final ObservableList<E> source;
    private final Subscription sourceSubscription;
    private final List<ListChangeListener<E>> listeners = new ArrayList<>();
    private boolean closed;

    /**
     * Creates an EDT-dispatching wrapper around {@code source}.
     *
     * @param source source observable list
     */
    public EdtObservableList(ObservableList<E> source) {
        this.source = Objects.requireNonNull(source, "source");
        this.sourceSubscription = source.subscribe(this::sourceChanged);
    }

    /**
     * Creates an EDT-dispatching wrapper around {@code source}.
     *
     * @param source source observable list
     * @param <E> element type
     * @return EDT-dispatching wrapper
     */
    public static <E> EdtObservableList<E> of(ObservableList<E> source) {
        return new EdtObservableList<>(source);
    }

    @Override
    public int size() {
        return source.size();
    }

    @Override
    public E get(int index) {
        return source.get(index);
    }

    @Override
    public List<E> snapshot() {
        return Collections.unmodifiableList(source.snapshot());
    }

    /**
     * Returns the wrapped source list.
     *
     * @return source list
     */
    public ObservableList<E> source() {
        return source;
    }

    @Override
    public void add(E element) {
        source.add(element);
    }

    @Override
    public void add(int index, E element) {
        source.add(index, element);
    }

    @Override
    public E set(int index, E element) {
        return source.set(index, element);
    }

    @Override
    public E remove(int index) {
        return source.remove(index);
    }

    @Override
    public void move(int fromIndex, int toIndex) {
        source.move(fromIndex, toIndex);
    }

    @Override
    public void clear() {
        source.clear();
    }

    @Override
    public void batch(Consumer<ObservableList<E>> work) {
        Objects.requireNonNull(work, "work");
        source.batch(ignored -> work.accept(this));
    }

    @Override
    public Subscription subscribe(ListChangeListener<E> listener) {
        Objects.requireNonNull(listener, "listener");
        synchronized (monitor) {
            if (closed) {
                return Subscription.of(() -> {
                });
            }
            listeners.add(listener);
        }
        return Subscription.of(() -> {
            synchronized (monitor) {
                listeners.remove(listener);
            }
        });
    }

    @Override
    public void close() {
        synchronized (monitor) {
            if (closed) {
                return;
            }
            closed = true;
            listeners.clear();
        }
        sourceSubscription.close();
    }

    private void sourceChanged(ListChangeSet<E> changes) {
        if (SwingUtilities.isEventDispatchThread()) {
            deliver(changes);
            return;
        }
        SwingUtilities.invokeLater(() -> deliver(changes));
    }

    private void deliver(ListChangeSet<E> changes) {
        List<ListChangeListener<E>> delivery;
        synchronized (monitor) {
            if (closed) {
                return;
            }
            delivery = List.copyOf(listeners);
        }
        for (ListChangeListener<E> listener : delivery) {
            listener.listChanged(changes);
        }
    }
}
