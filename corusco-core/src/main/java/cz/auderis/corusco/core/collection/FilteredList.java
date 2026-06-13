package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Read-only filtered view over an {@link ObservableList}.
 *
 * <p>The view subscribes to a source list, exposes only elements accepted by a
 * predicate, and emits change events with indices relative to the filtered
 * view. Dispatch is synchronous on the source-list mutating thread. Like the
 * source list primitives, this class is not synchronized.</p>
 *
 * <p>Direct mutation methods throw {@link UnsupportedOperationException}. The
 * source list remains the mutation owner so source indices, filtering, and
 * later transformed views stay coherent. Call {@link #close()} when the view is
 * no longer needed to release its source-list subscription.</p>
 *
 * @param <E> element type
 */
public final class FilteredList<E> implements ObservableList<E>, Disposable {

    private final List<ListChangeListener<E>> listeners = new ArrayList<>();
    private final Subscription subscription;
    private Predicate<? super E> predicate;
    private final List<E> sourceSnapshot;
    private List<E> visible;
    private boolean closed;

    /**
     * Creates a filtered view.
     *
     * @param source source list
     * @param predicate visibility predicate
     */
    public FilteredList(ObservableList<E> source, Predicate<? super E> predicate) {
        Objects.requireNonNull(source, "source");
        this.predicate = Objects.requireNonNull(predicate, "predicate");
        this.sourceSnapshot = new ArrayList<>(source.snapshot());
        this.visible = filter(sourceSnapshot);
        this.subscription = source.subscribe(this::sourceChanged);
    }

    /**
     * Creates a filtered view.
     *
     * @param source source list
     * @param predicate visibility predicate
     * @param <E> element type
     * @return filtered view
     */
    public static <E> FilteredList<E> of(ObservableList<E> source, Predicate<? super E> predicate) {
        return new FilteredList<>(source, predicate);
    }

    @Override
    public int size() {
        return visible.size();
    }

    @Override
    public E get(int index) {
        return visible.get(index);
    }

    @Override
    public List<E> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(visible));
    }

    /**
     * Replaces the visibility predicate and refreshes the view.
     *
     * <p>This early slice reports predicate replacement as a reset: removed
     * visible contents followed by inserted new visible contents where
     * applicable. Later diff-oriented stages may refine this into smaller
     * change sets.</p>
     *
     * @param predicate new visibility predicate
     */
    public void setPredicate(Predicate<? super E> predicate) {
        this.predicate = Objects.requireNonNull(predicate, "predicate");
        List<E> nextVisible = filter(sourceSnapshot);
        if (visible.equals(nextVisible)) {
            return;
        }
        List<ListChange<E>> changes = resetChanges(visible, nextVisible);
        visible = nextVisible;
        fireIfNotEmpty(changes);
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
        closed = true;
    }

    private void sourceChanged(ListChangeSet<E> changes) {
        if (closed) {
            return;
        }
        List<ListChange<E>> visibleChanges = new ArrayList<>();
        for (ListChange<E> change : changes.changes()) {
            apply(change, visibleChanges);
        }
        fireIfNotEmpty(visibleChanges);
    }

    private void apply(ListChange<E> change, List<ListChange<E>> visibleChanges) {
        switch (change) {
            case ListChange.Inserted<E> inserted -> applyInserted(inserted, visibleChanges);
            case ListChange.Removed<E> removed -> applyRemoved(removed, visibleChanges);
            case ListChange.Replaced<E> replaced -> applyReplaced(replaced, visibleChanges);
            case ListChange.Moved<E> moved -> applyMoved(moved, visibleChanges);
            case ListChange.Cleared<E> ignored -> applyCleared(visibleChanges);
        }
    }

    private void applyInserted(ListChange.Inserted<E> change, List<ListChange<E>> visibleChanges) {
        int visibleIndex = visibleIndexBeforeSourceIndex(change.index());
        List<E> insertedVisible = filter(change.elements());
        sourceSnapshot.addAll(change.index(), change.elements());
        if (insertedVisible.isEmpty()) {
            return;
        }
        visible.addAll(visibleIndex, insertedVisible);
        visibleChanges.add(new ListChange.Inserted<>(visibleIndex, insertedVisible));
    }

    private void applyRemoved(ListChange.Removed<E> change, List<ListChange<E>> visibleChanges) {
        int visibleIndex = visibleIndexBeforeSourceIndex(change.index());
        List<E> removedVisible = filter(change.elements());
        for (int i = 0; i < change.elements().size(); i++) {
            sourceSnapshot.remove(change.index());
        }
        if (removedVisible.isEmpty()) {
            return;
        }
        removeVisibleRange(visibleIndex, removedVisible.size());
        visibleChanges.add(new ListChange.Removed<>(visibleIndex, removedVisible));
    }

    private void applyReplaced(ListChange.Replaced<E> change, List<ListChange<E>> visibleChanges) {
        int visibleIndex = visibleIndexBeforeSourceIndex(change.index());
        boolean oldVisible = predicate.test(change.oldElement());
        boolean newVisible = predicate.test(change.newElement());
        sourceSnapshot.set(change.index(), change.newElement());
        if (oldVisible && newVisible) {
            visible.set(visibleIndex, change.newElement());
            visibleChanges.add(new ListChange.Replaced<>(visibleIndex, change.oldElement(), change.newElement()));
        } else if (oldVisible) {
            visible.remove(visibleIndex);
            visibleChanges.add(new ListChange.Removed<>(visibleIndex, singleton(change.oldElement())));
        } else if (newVisible) {
            visible.add(visibleIndex, change.newElement());
            visibleChanges.add(new ListChange.Inserted<>(visibleIndex, singleton(change.newElement())));
        }
    }

    private void applyMoved(ListChange.Moved<E> change, List<ListChange<E>> visibleChanges) {
        int fromVisible = visibleIndexBeforeSourceIndex(change.fromIndex());
        E moved = sourceSnapshot.remove(change.fromIndex());
        sourceSnapshot.add(change.toIndex(), moved);
        if (!predicate.test(moved)) {
            return;
        }
        E visibleElement = visible.remove(fromVisible);
        int toVisible = visibleIndexBeforeSourceIndex(change.toIndex());
        visible.add(toVisible, visibleElement);
        if (fromVisible != toVisible) {
            visibleChanges.add(new ListChange.Moved<>(fromVisible, toVisible, visibleElement));
        }
    }

    private void applyCleared(List<ListChange<E>> visibleChanges) {
        sourceSnapshot.clear();
        if (visible.isEmpty()) {
            return;
        }
        List<E> removedVisible = new ArrayList<>(visible);
        visible.clear();
        visibleChanges.add(new ListChange.Cleared<>(removedVisible));
    }

    private int visibleIndexBeforeSourceIndex(int sourceIndex) {
        int visibleIndex = 0;
        for (int i = 0; i < sourceIndex; i++) {
            if (predicate.test(sourceSnapshot.get(i))) {
                visibleIndex++;
            }
        }
        return visibleIndex;
    }

    private List<E> filter(List<E> elements) {
        List<E> result = new ArrayList<>();
        for (E element : elements) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }
        return result;
    }

    private List<ListChange<E>> resetChanges(List<E> previousVisible, List<E> nextVisible) {
        List<ListChange<E>> changes = new ArrayList<>(2);
        if (!previousVisible.isEmpty()) {
            changes.add(new ListChange.Cleared<>(previousVisible));
        }
        if (!nextVisible.isEmpty()) {
            changes.add(new ListChange.Inserted<>(0, nextVisible));
        }
        return changes;
    }

    private void removeVisibleRange(int index, int count) {
        for (int i = 0; i < count; i++) {
            visible.remove(index);
        }
    }

    private void fireIfNotEmpty(List<ListChange<E>> changes) {
        if (changes.isEmpty()) {
            return;
        }
        ListChangeSet<E> changeSet = new ListChangeSet<>(changes);
        List<ListChangeListener<E>> snapshot = List.copyOf(listeners);
        for (ListChangeListener<E> listener : snapshot) {
            listener.listChanged(changeSet);
        }
    }

    private UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException("FilteredList is a read-only view; mutate the source list");
    }

    private List<E> singleton(E element) {
        List<E> result = new ArrayList<>(1);
        result.add(element);
        return result;
    }
}
