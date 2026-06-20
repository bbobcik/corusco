package cz.auderis.corusco.glazedlists;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.util.concurrent.Lock;
import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableReadableCollection;
import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Read-only Glazed Lists mirror of an {@link ObservableReadableCollection}.
 *
 * <p>The mirror owns an internal mutable {@link BasicEventList}, exposes an
 * unmodifiable view to callers, and updates that list from Corusco change
 * events until closed. The returned event list preserves the source
 * collection's ordered coordinates, so inserts, removals, replacements, moves,
 * and clears are applied at the same indices reported by the source.</p>
 *
 * <p>The mirror is useful when a Glazed Lists consumer needs to observe data
 * owned by Corusco, including read-only sources such as sorted sets or mapped
 * projections. The mirror does not make the source mutable through Glazed
 * Lists; callers receive a read-only {@link EventList} view.</p>
 *
 * <p>Closing the mirror removes the Corusco subscription. It does not close or
 * clear the source collection, and it does not dispose the internal event list
 * view returned earlier to callers.</p>
 *
 * @param <E> element type
 */
public final class GlazedReadableCollectionMirror<E> implements Disposable {

    private final BasicEventList<E> mutableEventList;
    private final EventList<E> eventList;
    private final Subscription subscription;
    private boolean closed;

    /**
     * Creates a mirror for {@code source}.
     *
     * @param source source observable collection with non-null elements
     */
    public GlazedReadableCollectionMirror(ObservableReadableCollection<E> source) {
        Objects.requireNonNull(source, "source");
        this.mutableEventList = new BasicEventList<>(new ArrayList<>(source.snapshot()));
        this.eventList = GlazedLists.readOnlyList(mutableEventList);
        this.subscription = source.subscribe(this::sourceChanged);
    }

    /**
     * Creates a mirror for {@code source}.
     *
     * @param source source observable collection with non-null elements
     * @param <E> element type
     * @return mirror
     */
    public static <E> GlazedReadableCollectionMirror<E> of(ObservableReadableCollection<E> source) {
        return new GlazedReadableCollectionMirror<>(source);
    }

    /**
     * Returns the read-only mirrored event list.
     *
     * <p>The returned list is owned by this mirror. Mutate the Corusco source,
     * not the returned list, to publish changes.</p>
     *
     * @return read-only event list
     */
    public EventList<E> eventList() {
        return eventList;
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
        Lock lock = mutableEventList.getReadWriteLock().writeLock();
        lock.lock();
        try {
            for (ListChange<E> change : changes.changes()) {
                apply(change);
            }
        } finally {
            lock.unlock();
        }
    }

    private void apply(ListChange<E> change) {
        switch (change) {
            case ListChange.Inserted<E> inserted -> mutableEventList.addAll(inserted.index(), nonNull(inserted.elements()));
            case ListChange.Removed<E> removed -> removeRange(removed.index(), removed.elements().size());
            case ListChange.Replaced<E> replaced -> mutableEventList.set(
                    replaced.index(),
                    Objects.requireNonNull(replaced.newElement(), "element")
            );
            case ListChange.Moved<E> moved -> {
                E element = mutableEventList.remove(moved.fromIndex());
                mutableEventList.add(moved.toIndex(), element);
            }
            case ListChange.Cleared<E> ignored -> mutableEventList.clear();
        }
    }

    private void removeRange(int index, int count) {
        for (int i = 0; i < count; i++) {
            mutableEventList.remove(index);
        }
    }

    private List<E> nonNull(List<E> elements) {
        return List.copyOf(elements);
    }
}
