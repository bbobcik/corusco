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
 * <p>Single inserts, removals and replacements are reported in view coordinates.
 * A replacement that changes position produces one change set containing a
 * removal and an insertion. Compound source changes use a reset. Equal sort keys
 * retain source order. The comparator must remain consistent between source
 * events; call {@link #setComparator(Comparator)} after changing comparator
 * state or fields used by it, even when using the same comparator instance. Call
 * {@link #close()} when the view is no longer needed to release its source
 * subscription.</p>
 *
 * @param <E> element type
 */
public final class SortedList<E extends @NonNull Object> implements ObservableList<E>, Disposable {

    private final List<Entry<E>> sourceOrder = new ArrayList<>();
    private List<Entry<E>> sortedEntries;
    private final ListenerSet<ListChangeListener<E>, ListChangeSet<E>> listeners = new ListenerSet<>();
    private final Subscription subscription;
    private Comparator<? super E> comparator;
    private List<E> sorted;
    // Comparator failure can leave the mirror ahead of the published snapshot.
    private boolean needsRefresh;
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
        Objects.requireNonNull(source, "source");
        this.comparator = Objects.requireNonNull(comparator, "comparator");
        for (E element : source.snapshot()) sourceOrder.add(new Entry<>(element));
        this.sortedEntries = sortedEntries();
        this.sorted = values(sortedEntries);
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
        refresh(origin);
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
        if (closed) return;
        if (needsRefresh) {
            for (ListChange<E> change : changes.changes()) applySourceChange(change);
            refresh(changes.origin());
            return;
        }
        if (changes.changes().size() == 1) {
            ListChange<E> change = changes.changes().getFirst();
            if (change instanceof ListChange.Inserted<E> inserted && inserted.elements().size() == 1) {
                Entry<E> entry = new Entry<>(inserted.elements().getFirst());
                sourceOrder.add(inserted.index(), entry);
                needsRefresh = true;
                int index = insertionIndex(entry, inserted.index());
                sortedEntries.add(index, entry);
                sorted.add(index, entry.value);
                needsRefresh = false;
                fireIfNotEmpty(List.of(new ListChange.Inserted<>(index, List.of(entry.value))), changes.origin());
                return;
            }
            if (change instanceof ListChange.Removed<E> removed && removed.elements().size() == 1) {
                Entry<E> entry = sourceOrder.remove(removed.index());
                int index = sortedEntries.indexOf(entry);
                sortedEntries.remove(index);
                sorted.remove(index);
                fireIfNotEmpty(List.of(new ListChange.Removed<>(index, List.of(entry.value))), changes.origin());
                return;
            }
            if (change instanceof ListChange.Replaced<E> replaced) {
                Entry<E> old = sourceOrder.get(replaced.index());
                int oldIndex = sortedEntries.indexOf(old);
                Entry<E> next = new Entry<>(replaced.newElement());
                sourceOrder.set(replaced.index(), next);
                sortedEntries.remove(oldIndex);
                needsRefresh = true;
                int newIndex = insertionIndex(next, replaced.index());
                sortedEntries.add(newIndex, next);
                if (oldIndex == newIndex) {
                    sorted.set(oldIndex, next.value);
                    needsRefresh = false;
                    fireIfNotEmpty(List.of(new ListChange.Replaced<>(oldIndex, old.value, next.value)), changes.origin());
                } else {
                    sorted.remove(oldIndex);
                    sorted.add(newIndex, next.value);
                    needsRefresh = false;
                    fireIfNotEmpty(List.of(new ListChange.Removed<>(oldIndex, List.of(old.value)),
                            new ListChange.Inserted<>(newIndex, List.of(next.value))), changes.origin());
                }
                return;
            }
        }
        for (ListChange<E> change : changes.changes()) applySourceChange(change);
        refresh(changes.origin());
    }

    private void refresh(ChangeOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        needsRefresh = true;
        List<Entry<E>> nextEntries = sortedEntries();
        if (sortedEntries.equals(nextEntries)) {
            needsRefresh = false;
            return;
        }
        List<E> next = values(nextEntries);
        List<ListChange<E>> changes = resetChanges(sorted, next);
        sortedEntries = nextEntries;
        sorted = next;
        needsRefresh = false;
        fireIfNotEmpty(changes, origin);
    }

    private List<Entry<E>> sortedEntries() {
        List<Entry<E>> result = new ArrayList<>(sourceOrder);
        result.sort((left, right) -> comparator.compare(left.value, right.value));
        return result;
    }

    private List<E> values(List<Entry<E>> entries) {
        List<E> result = new ArrayList<>(entries.size());
        for (Entry<E> entry : entries) result.add(entry.value);
        return result;
    }

    private int insertionIndex(Entry<E> entry, int sourceIndex) {
        int low = 0, high = sortedEntries.size();
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (comparator.compare(sortedEntries.get(middle).value, entry.value) < 0) low = middle + 1;
            else high = middle;
        }
        if (low < sortedEntries.size() && comparator.compare(sortedEntries.get(low).value, entry.value) == 0) {
            for (int i = 0; i < sourceIndex; i++) {
                if (comparator.compare(sourceOrder.get(i).value, entry.value) == 0) low++;
            }
        }
        return low;
    }

    private void applySourceChange(ListChange<E> change) {
        switch (change) {
            case ListChange.Inserted<E> inserted -> {
                int index = inserted.index();
                for (E element : inserted.elements()) sourceOrder.add(index++, new Entry<>(element));
            }
            case ListChange.Removed<E> removed -> {
                sourceOrder.subList(removed.index(), removed.index() + removed.elements().size()).clear();
            }
            case ListChange.Replaced<E> replaced -> sourceOrder.set(replaced.index(), new Entry<>(replaced.newElement()));
            case ListChange.Moved<E> moved -> sourceOrder.add(moved.toIndex(), sourceOrder.remove(moved.fromIndex()));
            case ListChange.Cleared<E> ignored -> sourceOrder.clear();
        }
    }

    private static final class Entry<E> {
        private final E value;
        private Entry(E value) { this.value = value; }
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
