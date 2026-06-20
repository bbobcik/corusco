package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Read-only mapped view over an {@link ObservableReadableCollection}.
 *
 * <p>The view subscribes to a source collection, maps each source element with
 * a caller-provided function, and publishes translated changes in the same
 * ordered coordinates as the source. Mapper results must be non-null.</p>
 *
 * <p>The mapped collection keeps its own mapped snapshot so repeated indexed
 * reads do not call the mapper again. Source inserts, removals, replacements,
 * moves, and clears are applied to that snapshot and delivered to this view's
 * listeners as equivalent {@link ListChange} instances over mapped elements.</p>
 *
 * <p>Closing the view removes the source subscription and clears this view's
 * listener list. It does not close, clear, or otherwise own the source
 * collection.</p>
 *
 * @param <S> source element type
 * @param <T> mapped element type
 */
public class MappedReadableCollection<S, T> implements ObservableReadableCollection<T>, Disposable {

    private final Function<? super S, ? extends T> mapper;
    private final CopyOnWriteArrayList<ListChangeListener<T>> listeners = new CopyOnWriteArrayList<>();
    private final Subscription subscription;
    private List<T> mapped;
    private boolean closed;

    /**
     * Creates a mapped readable view.
     *
     * @param source source collection
     * @param mapper element mapper, returning non-null mapped values
     */
    public MappedReadableCollection(
            ObservableReadableCollection<S> source,
            Function<? super S, ? extends T> mapper
    ) {
        Objects.requireNonNull(source, "source");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.mapped = map(source.snapshot());
        this.subscription = source.subscribe(this::sourceChanged);
    }

    /**
     * Creates a mapped readable view.
     *
     * @param source source collection
     * @param mapper element mapper, returning non-null mapped values
     * @param <S> source element type
     * @param <T> mapped element type
     * @return mapped readable collection
     */
    public static <S, T> MappedReadableCollection<S, T> of(
            ObservableReadableCollection<S> source,
            Function<? super S, ? extends T> mapper
    ) {
        return new MappedReadableCollection<>(source, mapper);
    }

    @Override
    public int size() {
        return mapped.size();
    }

    @Override
    public T get(int index) {
        return mapped.get(index);
    }

    @Override
    public List<T> snapshot() {
        return List.copyOf(mapped);
    }

    @Override
    public Subscription subscribe(ListChangeListener<T> listener) {
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

    private void sourceChanged(ListChangeSet<S> sourceChanges) {
        if (closed) {
            return;
        }
        List<ListChange<T>> changes = new ArrayList<>();
        for (ListChange<S> sourceChange : sourceChanges.changes()) {
            apply(sourceChange, changes);
        }
        fireIfNotEmpty(changes);
    }

    private void apply(ListChange<S> sourceChange, List<ListChange<T>> changes) {
        switch (sourceChange) {
            case ListChange.Inserted<S> inserted -> applyInserted(inserted, changes);
            case ListChange.Removed<S> removed -> applyRemoved(removed, changes);
            case ListChange.Replaced<S> replaced -> applyReplaced(replaced, changes);
            case ListChange.Moved<S> moved -> applyMoved(moved, changes);
            case ListChange.Cleared<S> cleared -> applyCleared(cleared, changes);
        }
    }

    private void applyInserted(ListChange.Inserted<S> change, List<ListChange<T>> changes) {
        List<T> inserted = map(change.elements());
        mapped.addAll(change.index(), inserted);
        changes.add(new ListChange.Inserted<>(change.index(), inserted));
    }

    private void applyRemoved(ListChange.Removed<S> change, List<ListChange<T>> changes) {
        List<T> removed = removeRange(change.index(), change.elements().size());
        changes.add(new ListChange.Removed<>(change.index(), removed));
    }

    private void applyReplaced(ListChange.Replaced<S> change, List<ListChange<T>> changes) {
        T oldElement = mapped.set(change.index(), mapElement(change.newElement()));
        changes.add(new ListChange.Replaced<>(change.index(), oldElement, mapped.get(change.index())));
    }

    private void applyMoved(ListChange.Moved<S> change, List<ListChange<T>> changes) {
        T element = mapped.remove(change.fromIndex());
        mapped.add(change.toIndex(), element);
        changes.add(new ListChange.Moved<>(change.fromIndex(), change.toIndex(), element));
    }

    private void applyCleared(ListChange.Cleared<S> ignored, List<ListChange<T>> changes) {
        if (mapped.isEmpty()) {
            return;
        }
        List<T> removed = new ArrayList<>(mapped);
        mapped.clear();
        changes.add(new ListChange.Cleared<>(removed));
    }

    private List<T> map(List<S> sourceElements) {
        List<T> result = new ArrayList<>(sourceElements.size());
        for (S element : sourceElements) {
            result.add(mapElement(element));
        }
        return result;
    }

    private T mapElement(S element) {
        return Objects.requireNonNull(mapper.apply(element), "mapped element");
    }

    private List<T> removeRange(int index, int count) {
        List<T> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(mapped.remove(index));
        }
        return result;
    }

    private void fireIfNotEmpty(List<ListChange<T>> changes) {
        if (changes.isEmpty()) {
            return;
        }
        ListChangeSet<T> changeSet = new ListChangeSet<>(changes);
        for (ListChangeListener<T> listener : listeners) {
            listener.listChanged(changeSet);
        }
    }
}
