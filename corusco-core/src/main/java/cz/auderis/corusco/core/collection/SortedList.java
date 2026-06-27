package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;

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
public final class SortedList<E extends @NonNull Object> implements ObservableList<E>, Disposable {

    private final ObservableReadableCollection<E> source;
    private final ListenerSet<ListChangeListener<E>, ListChangeSet<E>> listeners = new ListenerSet<>();
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
    public SortedList(ObservableList<E> source, Comparator<? super E> comparator) {
        this((ObservableReadableCollection<E>) source, comparator);
    }

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
    public static <E extends @NonNull Object> SortedList<E> of(
            ObservableList<E> source,
            Comparator<? super E> comparator
    ) {
        return new SortedList<>(source, comparator);
    }

    /**
     * Creates a sorted view.
     *
     * @param source source collection
     * @param comparator sort comparator
     * @param <E> element type
     * @return sorted view
     */
    public static <E extends @NonNull Object> SortedList<E> of(
            ObservableReadableCollection<E> source,
            Comparator<? super E> comparator
    ) {
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

    @Override
    public Stream<E> stream() {
        return sorted.stream();
    }

    /**
     * Replaces the comparator and refreshes the sorted view.
     *
     * @param comparator new comparator
     */
    public void setComparator(Comparator<? super E> comparator) {
        setComparator(comparator, StandardChangeOrigin.MODEL);
    }

    /**
     * Replaces the comparator and refreshes the sorted view.
     *
     * @param comparator new comparator
     * @param origin change origin for a delivered reset
     */
    public void setComparator(Comparator<? super E> comparator, ChangeOrigin origin) {
        this.comparator = Objects.requireNonNull(comparator, "comparator");
        refreshFromSource(origin);
    }

    @Override
    public void add(E element, ChangeOrigin origin) {
        throw readOnly();
    }

    @Override
    public void add(int index, E element, ChangeOrigin origin) {
        throw readOnly();
    }

    @Override
    public E set(int index, E element, ChangeOrigin origin) {
        throw readOnly();
    }

    @Override
    public E remove(int index, ChangeOrigin origin) {
        throw readOnly();
    }

    @Override
    public void move(int fromIndex, int toIndex, ChangeOrigin origin) {
        throw readOnly();
    }

    @Override
    public void clear(ChangeOrigin origin) {
        throw readOnly();
    }

    @Override
    public void batch(ChangeOrigin origin, Consumer<ObservableList<E>> work) {
        throw readOnly();
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
        subscription.close();
        listeners.clearListeners();
        closed = true;
    }

    private void sourceChanged(ListChangeSet<E> changes) {
        if (!closed) {
            refreshFromSource(changes.origin());
        }
    }

    private void refreshFromSource(ChangeOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        List<E> next = sorted(source.snapshot());
        if (sorted.equals(next)) {
            return;
        }
        List<ListChange<E>> changes = resetChanges(sorted, next);
        sorted = next;
        fireIfNotEmpty(changes, origin);
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

    private void fireIfNotEmpty(List<ListChange<E>> changes, ChangeOrigin origin) {
        if (changes.isEmpty()) {
            return;
        }
        ListChangeSet<E> changeSet = new ListChangeSet<>(changes, origin);
        listeners.fireEvent(changeSet, ListChangeListener::listChanged);
    }

    private UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException("SortedList is a read-only view; mutate the source collection");
    }
}
