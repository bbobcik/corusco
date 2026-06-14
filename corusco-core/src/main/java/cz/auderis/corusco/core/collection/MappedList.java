package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Read-only mapped view over an {@link ObservableList}.
 *
 * <p>The view subscribes to a source list, maps each source element with a
 * caller-provided function, and publishes translated changes in the same order
 * as the source. It is useful when a presenter owns domain rows but a view
 * needs lightweight display rows, labels, or option objects without duplicating
 * source mutation logic.</p>
 *
 * <p>The mapper is invoked when the view is created and whenever source changes
 * arrive. Direct mutation methods throw {@link UnsupportedOperationException};
 * mutate the source list instead. Call {@link #close()} when the mapped view is
 * no longer needed to release its source-list subscription.</p>
 *
 * @param <S> source element type
 * @param <T> mapped element type
 */
public final class MappedList<S, T> implements ObservableList<T>, Disposable {

    private final Function<? super S, ? extends T> mapper;
    private final List<ListChangeListener<T>> listeners = new ArrayList<>();
    private final Subscription subscription;
    private List<T> mapped;
    private boolean closed;

    /**
     * Creates a mapped view.
     *
     * @param source source list
     * @param mapper element mapper
     */
    public MappedList(ObservableList<S> source, Function<? super S, ? extends T> mapper) {
        Objects.requireNonNull(source, "source");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.mapped = map(source.snapshot());
        this.subscription = source.subscribe(this::sourceChanged);
    }

    /**
     * Creates a mapped view.
     *
     * @param source source list
     * @param mapper element mapper
     * @param <S> source element type
     * @param <T> mapped element type
     * @return mapped view
     */
    public static <S, T> MappedList<S, T> of(
            ObservableList<S> source,
            Function<? super S, ? extends T> mapper
    ) {
        return new MappedList<>(source, mapper);
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
        return Collections.unmodifiableList(new ArrayList<>(mapped));
    }

    @Override
    public void add(T element) {
        throw readOnly();
    }

    @Override
    public void add(int index, T element) {
        throw readOnly();
    }

    @Override
    public T set(int index, T element) {
        throw readOnly();
    }

    @Override
    public T remove(int index) {
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
    public void batch(Consumer<ObservableList<T>> work) {
        throw readOnly();
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
        T oldElement = mapped.set(change.index(), mapper.apply(change.newElement()));
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
            result.add(mapper.apply(element));
        }
        return result;
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
        List<ListChangeListener<T>> snapshot = List.copyOf(listeners);
        for (ListChangeListener<T> listener : snapshot) {
            listener.listChanged(changeSet);
        }
    }

    private UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException("MappedList is a read-only view; mutate the source list");
    }
}
