package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Read-only sorted view over an {@link ObservableReadableCollection}.
 *
 * <p>The view subscribes to a source collection, stores a sorted snapshot, and emits
 * changes with indices relative to the sorted view. Source ownership remains
 * unchanged: callers mutate the source collection owner, while this view
 * provides stable sorted reads for presentation code, adapters, and tests that
 * should not own collection storage.</p>
 *
 * <p>Sorting makes many source mutations ambiguous to translate as a precise
 * single insert, remove, replace, or move in view coordinates. For deterministic
 * behavior, every visible content change is therefore reported as a reset:
 * cleared previous contents followed by inserted current contents where
 * applicable. Comparator replacement uses the same reset rule. Call
 * {@link #close()} when the view is no longer needed to release its source
 * subscription.</p>
 *
 * @param <E> element type
 */
public final class SortedList<E> implements ObservableList<E>, Disposable {

    private final ObservableReadableCollection<E> source;
    private final CopyOnWriteArrayList<ListChangeListener<E>> listeners = new CopyOnWriteArrayList<>();
    private final Subscription subscription;
    private Comparator<? super E> comparator;
    private List<E> sorted;
    private boolean closed;

    /**
     * Creates a sorted view.
     *
     * @param source source collection
     * @param comparator sort comparator
     */
    public SortedList(ObservableReadableCollection<E> source, Comparator<? super E> comparator) {
        this.source = Objects.requireNonNull(source, "source");
        this.comparator = Objects.requireNonNull(comparator, "comparator");
        this.sorted = sorted(source.snapshot());
        this.subscription = source.subscribe(this::sourceChanged);
    }

    /**
     * Creates a sorted view.
     *
     * @param source source collection
     * @param comparator sort comparator
     * @param <E> element type
     * @return sorted view
     */
    public static <E> SortedList<E> of(ObservableReadableCollection<E> source, Comparator<? super E> comparator) {
        return new SortedList<>(source, comparator);
    }

    @Override
    public int size() {
        return sorted.size();
    }

    @Override
    public E get(int index) {
        return sorted.get(index);
    }

    @Override
    public List<E> snapshot() {
        return List.copyOf(sorted);
    }

    /**
     * Replaces the comparator and refreshes the sorted view.
     *
     * @param comparator new comparator
     */
    public void setComparator(Comparator<? super E> comparator) {
        this.comparator = Objects.requireNonNull(comparator, "comparator");
        refreshFromSource();
    }

    @Override
    public void add(E element) {
        throw readOnly();
    }

    @Override
    public void add(int index, E element) {
        throw readOnly();
    }

    @Override
    public E set(int index, E element) {
        throw readOnly();
    }

    @Override
    public E remove(int index) {
        throw readOnly();
    }

    @Override
    public void move(int fromIndex, int toIndex) {
        throw readOnly();
    }

    @Override
    public void clear() {
        throw readOnly();
    }

    @Override
    public void batch(Consumer<ObservableList<E>> work) {
        throw readOnly();
    }

    @Override
    public Subscription subscribe(ListChangeListener<E> listener) {
        Objects.requireNonNull(listener, "listener");
        listeners.add(listener);
        return Subscription.of(() -> listeners.remove(listener));
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        subscription.close();
        listeners.clear();
        closed = true;
    }

    private void sourceChanged(ListChangeSet<E> ignored) {
        if (!closed) {
            refreshFromSource();
        }
    }

    private void refreshFromSource() {
        List<E> next = sorted(source.snapshot());
        if (sorted.equals(next)) {
            return;
        }
        List<ListChange<E>> changes = resetChanges(sorted, next);
        sorted = next;
        fireIfNotEmpty(changes);
    }

    private List<E> sorted(List<E> elements) {
        List<E> result = new ArrayList<>(elements);
        result.sort(comparator);
        return result;
    }

    private List<ListChange<E>> resetChanges(List<E> previous, List<E> next) {
        List<ListChange<E>> changes = new ArrayList<>(2);
        if (!previous.isEmpty()) {
            changes.add(new ListChange.Cleared<>(previous));
        }
        if (!next.isEmpty()) {
            changes.add(new ListChange.Inserted<>(0, next));
        }
        return changes;
    }

    private void fireIfNotEmpty(List<ListChange<E>> changes) {
        if (changes.isEmpty()) {
            return;
        }
        ListChangeSet<E> changeSet = new ListChangeSet<>(changes);
        for (ListChangeListener<E> listener : listeners) {
            listener.listChanged(changeSet);
        }
    }

    private UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException("SortedList is a read-only view; mutate the source collection");
    }
}
