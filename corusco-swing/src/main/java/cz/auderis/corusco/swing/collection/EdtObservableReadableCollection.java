package cz.auderis.corusco.swing.collection;

import cz.auderis.corusco.core.collection.ListChangeListener;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableReadableCollection;
import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

/**
 * {@link ObservableReadableCollection} wrapper that delivers listener callbacks
 * on the EDT.
 *
 * <p>The wrapper delegates reads directly to the source collection and
 * subscribes to source changes immediately. If a source change arrives on a
 * background thread, delivery to this wrapper's listeners is scheduled with
 * {@link SwingUtilities#invokeLater(Runnable)}. Changes already delivered on
 * the EDT are forwarded synchronously.</p>
 *
 * <p>Closing the wrapper removes the source subscription and clears this
 * wrapper's listener list. It does not close, clear, or otherwise own the
 * source collection. Subscribers added after close receive an inert
 * subscription.</p>
 *
 * @param <E> element type
 */
public final class EdtObservableReadableCollection<E> implements ObservableReadableCollection<E>, Disposable {

    private final Object monitor = new Object();
    private final ObservableReadableCollection<E> source;
    private final Subscription sourceSubscription;
    private final CopyOnWriteArrayList<ListChangeListener<E>> listeners = new CopyOnWriteArrayList<>();
    private boolean closed;

    /**
     * Creates an EDT-dispatching wrapper around {@code source}.
     *
     * @param source source observable collection
     */
    public EdtObservableReadableCollection(ObservableReadableCollection<E> source) {
        this.source = Objects.requireNonNull(source, "source");
        this.sourceSubscription = source.subscribe(this::sourceChanged);
    }

    /**
     * Creates an EDT-dispatching wrapper around {@code source}.
     *
     * @param source source observable collection
     * @param <E> element type
     * @return EDT-dispatching wrapper
     */
    public static <E> EdtObservableReadableCollection<E> of(ObservableReadableCollection<E> source) {
        return new EdtObservableReadableCollection<>(source);
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
        return source.snapshot();
    }

    /**
     * Returns the wrapped source collection.
     *
     * @return source collection
     */
    public ObservableReadableCollection<E> source() {
        return source;
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
        synchronized (monitor) {
            if (closed) {
                return;
            }
        }
        for (ListChangeListener<E> listener : listeners) {
            listener.listChanged(changes);
        }
    }
}
