package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Detachable;
import cz.auderis.corusco.core.lifecycle.Subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Supplier-backed observable list that loads lazily and detaches its cache.
 *
 * <p>The loader is invoked on the caller's thread. Loaded elements are copied
 * into an internal {@link ObservableArrayList}, so subsequent list mutations
 * affect the attached presentation cache rather than writing back through the
 * loader. This class is not synchronized and follows the collection package's
 * single-owner presentation-state convention.</p>
 *
 * @param <E> element type
 */
public final class LoadableList<E> implements ObservableList<E>, Detachable {

    private final Supplier<? extends Collection<? extends E>> loader;
    private final List<ListChangeListener<E>> listeners = new ArrayList<>();
    private ObservableArrayList<E> cache;
    private Subscription cacheSubscription;

    /**
     * Creates a lazy observable list.
     *
     * @param loader loader invoked when rows are first needed after
     *         construction or detach
     */
    public LoadableList(Supplier<? extends Collection<? extends E>> loader) {
        this.loader = Objects.requireNonNull(loader, "loader");
    }

    /**
     * Creates a lazy observable list.
     *
     * @param loader loader invoked when rows are first needed after
     *         construction or detach
     * @param <E> element type
     * @return loadable list
     */
    public static <E> LoadableList<E> of(Supplier<? extends Collection<? extends E>> loader) {
        return new LoadableList<>(loader);
    }

    /**
     * Indicates whether this list currently holds an attached row cache.
     *
     * @return {@code true} when cached rows are present
     */
    public boolean isAttached() {
        return cache != null;
    }

    @Override
    public int size() {
        return attached().size();
    }

    @Override
    public E get(int index) {
        return attached().get(index);
    }

    @Override
    public List<E> snapshot() {
        return attached().snapshot();
    }

    @Override
    public void add(E element) {
        attached().add(element);
    }

    @Override
    public void add(int index, E element) {
        attached().add(index, element);
    }

    @Override
    public E set(int index, E element) {
        return attached().set(index, element);
    }

    @Override
    public E remove(int index) {
        return attached().remove(index);
    }

    @Override
    public void move(int fromIndex, int toIndex) {
        attached().move(fromIndex, toIndex);
    }

    @Override
    public void clear() {
        attached().clear();
    }

    @Override
    public void batch(Consumer<ObservableList<E>> work) {
        Objects.requireNonNull(work, "work");
        attached().batch(ignored -> work.accept(this));
    }

    @Override
    public Subscription subscribe(ListChangeListener<E> listener) {
        Objects.requireNonNull(listener, "listener");
        listeners.add(listener);
        return Subscription.of(() -> listeners.remove(listener));
    }

    @Override
    public void detach() {
        invalidate();
    }

    /**
     * Releases cached rows without eagerly loading replacements.
     */
    public void invalidate() {
        if (cacheSubscription != null) {
            cacheSubscription.close();
            cacheSubscription = null;
        }
        cache = null;
    }

    /**
     * Reloads rows and emits a reset-style change set when the visible snapshot
     * changes.
     *
     * @return refreshed immutable snapshot
     */
    public List<E> refresh() {
        List<E> oldSnapshot = isAttached() ? cache.snapshot() : List.of();
        boolean hadAttachedCache = isAttached();
        List<E> newSnapshot = loadSnapshot();
        replaceCache(newSnapshot);
        if (hadAttachedCache && !oldSnapshot.equals(newSnapshot)) {
            fire(resetChanges(oldSnapshot, newSnapshot));
        }
        return cache.snapshot();
    }

    private ObservableArrayList<E> attached() {
        if (cache == null) {
            replaceCache(loadSnapshot());
        }
        return cache;
    }

    private List<E> loadSnapshot() {
        Collection<? extends E> loaded = Objects.requireNonNull(loader.get(), "loaded elements");
        return Collections.unmodifiableList(new ArrayList<>(loaded));
    }

    private void replaceCache(Collection<? extends E> elements) {
        if (cacheSubscription != null) {
            cacheSubscription.close();
        }
        cache = ObservableArrayList.of(elements);
        cacheSubscription = cache.subscribe(this::fire);
    }

    private ListChangeSet<E> resetChanges(List<E> oldSnapshot, List<E> newSnapshot) {
        List<ListChange<E>> changes = new ArrayList<>(2);
        if (!oldSnapshot.isEmpty()) {
            changes.add(new ListChange.Cleared<>(oldSnapshot));
        }
        if (!newSnapshot.isEmpty()) {
            changes.add(new ListChange.Inserted<>(0, newSnapshot));
        }
        return new ListChangeSet<>(changes);
    }

    private void fire(ListChangeSet<E> changeSet) {
        List<ListChangeListener<E>> snapshot = List.copyOf(listeners);
        for (ListChangeListener<E> listener : snapshot) {
            listener.listChanged(changeSet);
        }
    }
}
