package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Detachable;
import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;

/**
 * Supplier-backed observable list that loads lazily and detaches its cache.
 *
 * <p>This class is useful when a presenter owns a row list whose data can be
 * released between activations and reloaded later. The first read or mutation
 * after construction or {@link #detach()} invokes the supplied loader, copies
 * the loaded elements into an internal {@link ObservableArrayList}, and then
 * serves list operations from that cache. Subsequent mutations affect the
 * attached presentation cache; they do not write back through the loader.</p>
 *
 * <p>The loader is invoked on the caller's thread and must not return
 * {@code null}. The list retains subscribers across detach/refresh operations.
 * {@link #refresh()} reloads data and emits a reset-style change set only when
 * a previously attached snapshot changes. {@link #detach()} and
 * {@link #invalidate()} release cached rows without firing list changes.</p>
 *
 * <p>This class is not synchronized and follows the collection package's
 * single-owner presentation-state convention. If the list is bound to Swing,
 * perform load, refresh, and mutation work according to the Swing adapter's EDT
 * rules.</p>
 *
 * @param <E> element type
 */
public final class LoadableList<E extends @NonNull Object> implements ObservableList<E>, Detachable {

    private final Supplier<? extends Collection<? extends E>> loader;
    private final ListenerSet<ListChangeListener<E>, ListChangeSet<E>> listeners = new ListenerSet<>();
    private @Nullable ObservableArrayList<E> cache;
    private @Nullable Subscription cacheSubscription;

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
    public static <E extends @NonNull Object> LoadableList<E> of(
            Supplier<? extends Collection<? extends E>> loader
    ) {
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
    public Stream<E> stream() {
        return attached().stream();
    }

    @Override
    public void add(E element, ChangeOrigin origin) {
        attached().add(element, origin);
    }

    @Override
    public void add(int index, E element, ChangeOrigin origin) {
        attached().add(index, element, origin);
    }

    @Override
    public E set(int index, E element, ChangeOrigin origin) {
        return attached().set(index, element, origin);
    }

    @Override
    public E remove(int index, ChangeOrigin origin) {
        return attached().remove(index, origin);
    }

    @Override
    public void move(int fromIndex, int toIndex, ChangeOrigin origin) {
        attached().move(fromIndex, toIndex, origin);
    }

    @Override
    public void clear(ChangeOrigin origin) {
        attached().clear(origin);
    }

    @Override
    public void batch(ChangeOrigin origin, Consumer<ObservableList<E>> work) {
        Objects.requireNonNull(work, "work");
        attached().batch(origin, ignored -> work.accept(this));
    }

    @Override
    public Subscription subscribe(ListChangeListener<E> listener) {
        return listeners.addListener(listener);
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
        return refresh(StandardChangeOrigin.MODEL);
    }

    /**
     * Reloads rows and emits a reset-style change set when the visible snapshot
     * changes.
     *
     * @param origin change origin for a delivered reset
     * @return refreshed immutable snapshot
     */
    public List<E> refresh(ChangeOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        List<E> oldSnapshot = isAttached() ? cache.snapshot() : List.of();
        boolean hadAttachedCache = isAttached();
        List<E> newSnapshot = loadSnapshot();
        replaceCache(newSnapshot);
        if (hadAttachedCache && !oldSnapshot.equals(newSnapshot)) {
            fire(resetChanges(oldSnapshot, newSnapshot, origin));
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
        return List.copyOf(loaded);
    }

    private void replaceCache(Collection<? extends E> elements) {
        if (cacheSubscription != null) {
            cacheSubscription.close();
        }
        cache = ObservableArrayList.of(elements);
        cacheSubscription = cache.subscribe(this::fire);
    }

    private ListChangeSet<E> resetChanges(List<E> oldSnapshot, List<E> newSnapshot, ChangeOrigin origin) {
        List<ListChange<E>> changes = new ArrayList<>(2);
        if (!oldSnapshot.isEmpty()) {
            changes.add(new ListChange.Cleared<>(oldSnapshot));
        }
        if (!newSnapshot.isEmpty()) {
            changes.add(new ListChange.Inserted<>(0, newSnapshot));
        }
        return new ListChangeSet<>(changes, origin);
    }

    private void fire(ListChangeSet<E> changeSet) {
        listeners.fireEvent(changeSet, ListChangeListener::listChanged);
    }
}
