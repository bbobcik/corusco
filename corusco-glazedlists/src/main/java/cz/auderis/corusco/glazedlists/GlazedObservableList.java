package cz.auderis.corusco.glazedlists;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.util.concurrent.Lock;
import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ListChangeListener;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableList;
import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Corusco {@link ObservableList} view over a Glazed Lists {@link EventList}.
 *
 * <p>The wrapped event list remains the storage owner. Mutating methods
 * delegate to the event list and Glazed Lists events are translated to Corusco
 * {@link ListChange} deliveries. Public reads and direct adapter mutations use
 * the event list's read/write lock. External Glazed Lists callers should follow
 * Glazed Lists' own locking rules when mutating the same list.</p>
 *
 * <p>Closing the adapter removes its Glazed Lists listener. The adapter does
 * not dispose the wrapped {@code EventList}; ownership of that list remains
 * with the caller.</p>
 *
 * <p>{@link #batch(Consumer)} keeps Corusco listener delivery batched into one
 * change set. Glazed Lists still controls the underlying event-list mutation
 * semantics.</p>
 *
 * @param <E> element type
 */
public final class GlazedObservableList<E> implements ObservableList<E>, Disposable {

    private final EventList<E> source;
    private final ListenerSet<ListChangeListener<E>, ListChangeSet<E>> listeners = new ListenerSet<>();
    private final List<ListChange<E>> batchedChanges = new ArrayList<>();
    private final ListEventListener<E> sourceListener = this::sourceChanged;
    private List<E> sourceSnapshot;
    private int batchDepth;
    private boolean closed;
    private boolean suppressSourceEvents;

    /**
     * Creates an adapter around a Glazed Lists event list.
     *
     * @param source wrapped event list
     */
    public GlazedObservableList(EventList<E> source) {
        this.source = Objects.requireNonNull(source, "source");
        this.sourceSnapshot = snapshotSource();
        this.source.addListEventListener(sourceListener);
    }

    /**
     * Creates an adapter around a Glazed Lists event list.
     *
     * @param source wrapped event list
     * @param <E> element type
     * @return adapter
     */
    public static <E> GlazedObservableList<E> of(EventList<E> source) {
        return new GlazedObservableList<>(source);
    }

    @Override
    public int size() {
        Lock lock = source.getReadWriteLock().readLock();
        lock.lock();
        try {
            return source.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E get(int index) {
        Lock lock = source.getReadWriteLock().readLock();
        lock.lock();
        try {
            return source.get(index);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<E> snapshot() {
        return List.copyOf(snapshotSource());
    }

    /**
     * Returns the wrapped Glazed Lists event list.
     *
     * @return source event list
     */
    public EventList<E> source() {
        return source;
    }

    @Override
    public void add(@NonNull E element) {
        Objects.requireNonNull(element, "element");
        Lock lock = source.getReadWriteLock().writeLock();
        lock.lock();
        try {
            source.add(element);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void add(int index, E element) {
        Objects.requireNonNull(element, "element");
        Lock lock = source.getReadWriteLock().writeLock();
        lock.lock();
        try {
            source.add(index, element);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E set(int index, E element) {
        Objects.requireNonNull(element, "element");
        Lock lock = source.getReadWriteLock().writeLock();
        lock.lock();
        try {
            return source.set(index, element);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E remove(int index) {
        Lock lock = source.getReadWriteLock().writeLock();
        lock.lock();
        try {
            return source.remove(index);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void move(int fromIndex, int toIndex) {
        if (fromIndex == toIndex) {
            return;
        }
        Lock lock = source.getReadWriteLock().writeLock();
        lock.lock();
        E moved;
        try {
            suppressSourceEvents = true;
            moved = source.remove(fromIndex);
            source.add(toIndex, moved);
            sourceSnapshot = new ArrayList<>(source);
        } finally {
            suppressSourceEvents = false;
            lock.unlock();
        }
        record(new ListChange.Moved<>(fromIndex, toIndex, moved));
    }

    @Override
    public void clear() {
        Lock lock = source.getReadWriteLock().writeLock();
        lock.lock();
        try {
            source.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void batch(Consumer<ObservableList<E>> work) {
        Objects.requireNonNull(work, "work");
        batchDepth++;
        try {
            work.accept(this);
        } finally {
            batchDepth--;
            if (batchDepth == 0 && !batchedChanges.isEmpty()) {
                List<ListChange<E>> delivery = new ArrayList<>(batchedChanges);
                batchedChanges.clear();
                fire(new ListChangeSet<>(delivery));
            }
        }
    }

    @Override
    public Subscription subscribe(ListChangeListener<E> listener) {
        return listeners.addListener(listener);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        source.removeListEventListener(sourceListener);
        closed = true;
    }

    private void sourceChanged(ListEvent<E> event) {
        if (closed) {
            return;
        }
        if (suppressSourceEvents) {
            sourceSnapshot = new ArrayList<>(source);
            return;
        }
        if (event.isReordering()) {
            applyReorder();
            return;
        }
        List<ListChange<E>> changes = new ArrayList<>();
        while (event.next()) {
            switch (event.getType()) {
                case ListEvent.INSERT -> applyInsert(event.getIndex(), changes);
                case ListEvent.DELETE -> applyDelete(event.getIndex(), changes);
                case ListEvent.UPDATE -> applyUpdate(event.getIndex(), changes);
                default -> throw new IllegalStateException("Unknown Glazed Lists event type: " + event.getType());
            }
        }
        fireIfNotEmpty(changes);
    }

    private void applyInsert(int index, List<ListChange<E>> changes) {
        E element = Objects.requireNonNull(source.get(index), "element");
        sourceSnapshot.add(index, element);
        changes.add(new ListChange.Inserted<>(index, singleton(element)));
    }

    private void applyDelete(int index, List<ListChange<E>> changes) {
        E oldElement = sourceSnapshot.remove(index);
        changes.add(new ListChange.Removed<>(index, singleton(oldElement)));
    }

    private void applyUpdate(int index, List<ListChange<E>> changes) {
        E oldElement = sourceSnapshot.set(index, Objects.requireNonNull(source.get(index), "element"));
        changes.add(new ListChange.Replaced<>(index, oldElement, sourceSnapshot.get(index)));
    }

    private void applyReorder() {
        List<E> previous = new ArrayList<>(sourceSnapshot);
        sourceSnapshot = new ArrayList<>(source);
        List<ListChange<E>> changes = new ArrayList<>(2);
        if (!previous.isEmpty()) {
            changes.add(new ListChange.Cleared<>(previous));
        }
        if (!sourceSnapshot.isEmpty()) {
            changes.add(new ListChange.Inserted<>(0, sourceSnapshot));
        }
        fireIfNotEmpty(changes);
    }

    private List<E> snapshotSource() {
        Lock lock = source.getReadWriteLock().readLock();
        lock.lock();
        try {
            return new ArrayList<>(List.copyOf(source));
        } finally {
            lock.unlock();
        }
    }

    private void fireIfNotEmpty(List<ListChange<E>> changes) {
        if (changes.isEmpty()) {
            return;
        }
        record(changes);
    }

    private void record(ListChange<E> change) {
        record(Collections.singletonList(change));
    }

    private void record(List<ListChange<E>> changes) {
        if (batchDepth > 0) {
            batchedChanges.addAll(changes);
            return;
        }
        fire(new ListChangeSet<>(changes));
    }

    private void fire(ListChangeSet<E> changeSet) {
        listeners.fireEvent(changeSet, ListChangeListener::listChanged);
    }

    private List<E> singleton(E element) {
        List<E> result = new ArrayList<>(1);
        result.add(element);
        return result;
    }
}
