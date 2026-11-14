package cz.auderis.corusco.swing.collection;

import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ListChangeListener;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableReadableCollection;
import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Shared EDT delivery engine of the EDT collection wrappers.
 *
 * <p>The relay keeps an EDT-confined copy of the source elements. The copy is
 * initialised from the source snapshot and afterwards changes only when a
 * source change set is delivered to wrapper listeners on the EDT, so EDT reads
 * always agree with the events EDT listeners have received. Change sets are
 * delivered strictly in the order the source raised them, whether they arrived
 * on the EDT or on a background thread.</p>
 *
 * @param <E> element type
 */
final class EdtChangeRelay<E extends @NonNull Object> {

    private final Object monitor = new Object();
    private final ObservableReadableCollection<E> source;
    private final ArrayList<E> elements;
    private final ConcurrentLinkedQueue<ListChangeSet<E>> pending = new ConcurrentLinkedQueue<>();
    private final ListenerSet<ListChangeListener<E>, ListChangeSet<E>> listeners = new ListenerSet<>();
    private final Subscription sourceSubscription;
    private volatile boolean closed;
    // EDT-confined state
    private boolean draining;
    private int sourceReadDepth;

    EdtChangeRelay(ObservableReadableCollection<E> source) {
        this.source = Objects.requireNonNull(source, "source");
        this.elements = new ArrayList<>(source.snapshot());
        this.sourceSubscription = source.subscribe(this::sourceChanged);
    }

    ObservableReadableCollection<E> source() {
        return source;
    }

    int size() {
        return readsCopy() ? elements.size() : source.size();
    }

    E get(int index) {
        return readsCopy() ? elements.get(index) : source.get(index);
    }

    List<E> snapshot() {
        return readsCopy() ? List.copyOf(elements) : Collections.unmodifiableList(source.snapshot());
    }

    Stream<E> stream() {
        return readsCopy() ? List.copyOf(elements).stream() : source.stream();
    }

    /**
     * Runs {@code work} with EDT reads redirected to the live source, used by
     * batches whose changes are not delivered until the batch completes.
     */
    void withSourceReads(Runnable work) {
        boolean onEdt = SwingUtilities.isEventDispatchThread();
        if (onEdt) {
            sourceReadDepth++;
        }
        try {
            work.run();
        } finally {
            if (onEdt) {
                sourceReadDepth--;
            }
        }
    }

    Subscription subscribe(ListChangeListener<E> listener) {
        synchronized (monitor) {
            if (closed) {
                return Subscription.EMPTY;
            }
            return listeners.addListener(listener);
        }
    }

    void close() {
        synchronized (monitor) {
            if (closed) {
                return;
            }
            closed = true;
            listeners.clearListeners();
        }
        sourceSubscription.close();
        pending.clear();
    }

    private boolean readsCopy() {
        return !closed && sourceReadDepth == 0 && SwingUtilities.isEventDispatchThread();
    }

    private void sourceChanged(ListChangeSet<E> changes) {
        pending.add(changes);
        if (SwingUtilities.isEventDispatchThread()) {
            drain();
        } else {
            SwingUtilities.invokeLater(this::drain);
        }
    }

    private void drain() {
        if (draining) {
            // A listener changed the source during delivery; the running drain
            // loop delivers the queued change after the current one.
            return;
        }
        draining = true;
        try {
            @Nullable ListChangeSet<E> changes;
            while (null != (changes = pending.poll())) {
                if (closed) {
                    pending.clear();
                    return;
                }
                apply(changes);
                listeners.fireEvent(changes, ListChangeListener::listChanged);
            }
        } finally {
            draining = false;
        }
    }

    private void apply(ListChangeSet<E> changes) {
        for (ListChange<E> change : changes.changes()) {
            switch (change) {
                case ListChange.Inserted<E> inserted -> elements.addAll(inserted.index(), inserted.elements());
                case ListChange.Removed<E> removed -> elements
                        .subList(removed.index(), removed.index() + removed.elements().size())
                        .clear();
                case ListChange.Replaced<E> replaced -> elements.set(replaced.index(), replaced.newElement());
                case ListChange.Moved<E> moved -> elements.add(moved.toIndex(), elements.remove(moved.fromIndex()));
                case ListChange.Cleared<E> ignored -> elements.clear();
            }
        }
    }
}
