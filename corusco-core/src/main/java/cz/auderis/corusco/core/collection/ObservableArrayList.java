package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.value.ChangeOrigin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;

/**
 * Mutable {@link ObservableList} implementation backed by an {@link ArrayList}.
 *
 * <p>This class is the general-purpose observable collection used by examples,
 * bindings, and adapters when no specialized list implementation is needed. It
 * owns its element storage, reports every structural mutation as a
 * {@link ListChangeSet}, and returns immutable snapshots so callers can inspect
 * list contents without holding an internal reference.</p>
 *
 * <p>Listeners are retained until the returned subscription is disposed.
 * Notifications are delivered synchronously on the thread that performs the
 * mutation. The class performs no synchronization and has no Swing awareness;
 * callers that expose it to Swing components should mutate it on the event
 * dispatch thread or use an adapter that documents a different policy.</p>
 *
 * @param <E> element type
 */
public final class ObservableArrayList<E extends @NonNull Object> implements ObservableList<E> {

    private final List<E> elements = new ArrayList<>();
    private final ListenerSet<ListChangeListener<E>, ListChangeSet<E>> listeners = new ListenerSet<>();
    private final List<ListChange<E>> batchedChanges = new ArrayList<>();
    private int batchDepth;
    private ChangeOrigin batchOrigin;

    /**
     * Creates an empty observable list.
     */
    public ObservableArrayList() {
    }

    /**
     * Creates an observable list with initial contents.
     *
     * <p>The supplied collection is copied during construction. Later changes
     * to the collection are not observed, and construction itself does not fire
     * change events.</p>
     *
     * @param initialElements initial elements, not {@code null}
     */
    public ObservableArrayList(Collection<? extends E> initialElements) {
        elements.addAll(List.copyOf(Objects.requireNonNull(initialElements, "initialElements")));
    }

    /**
     * Creates an empty observable list.
     *
     * @param <E> element type
     * @return observable list
     */
    public static <E extends @NonNull Object> ObservableArrayList<E> empty() {
        return new ObservableArrayList<>();
    }

    /**
     * Creates an observable list with initial contents.
     *
     * @param elements initial elements, copied into the new list
     * @param <E> element type
     * @return observable list
     */
    public static <E extends @NonNull Object> ObservableArrayList<E> of(Collection<? extends E> elements) {
        return new ObservableArrayList<>(elements);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public E get(int index) {
        return elements.get(index);
    }

    @Override
    public List<E> snapshot() {
        return List.copyOf(elements);
    }

    @Override
    public Stream<E> stream() {
        return elements.stream();
    }

    @Override
    public void add(E element, ChangeOrigin origin) {
        add(elements.size(), element, origin);
    }

    @Override
    public void add(int index, E element, ChangeOrigin origin) {
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(origin, "origin");
        elements.add(index, element);
        record(new ListChange.Inserted<>(index, singleton(element)), origin);
    }

    @Override
    public E set(int index, E element, ChangeOrigin origin) {
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(origin, "origin");
        E oldElement = elements.set(index, element);
        record(new ListChange.Replaced<>(index, oldElement, element), origin);
        return oldElement;
    }

    @Override
    public E remove(int index, ChangeOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        E removed = elements.remove(index);
        record(new ListChange.Removed<>(index, singleton(removed)), origin);
        return removed;
    }

    @Override
    public void move(int fromIndex, int toIndex, ChangeOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        if (fromIndex == toIndex) {
            return;
        }
        E element = elements.remove(fromIndex);
        elements.add(toIndex, element);
        record(new ListChange.Moved<>(fromIndex, toIndex, element), origin);
    }

    @Override
    public void clear(ChangeOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        if (elements.isEmpty()) {
            return;
        }
        List<E> removed = new ArrayList<>(elements);
        elements.clear();
        record(new ListChange.Cleared<>(removed), origin);
    }

    @Override
    public void batch(ChangeOrigin origin, java.util.function.Consumer<ObservableList<E>> work) {
        Objects.requireNonNull(work, "work");
        Objects.requireNonNull(origin, "origin");
        ChangeOrigin previousOrigin = batchOrigin;
        if (batchDepth == 0) {
            batchOrigin = origin;
        }
        batchDepth++;
        try {
            work.accept(this);
        } finally {
            batchDepth--;
            if (batchDepth == 0 && !batchedChanges.isEmpty()) {
                List<ListChange<E>> delivery = new ArrayList<>(batchedChanges);
                batchedChanges.clear();
                fire(new ListChangeSet<>(delivery, batchOrigin));
            }
            if (batchDepth == 0) {
                batchOrigin = previousOrigin;
            }
        }
    }

    @Override
    public Subscription subscribe(ListChangeListener<E> listener) {
        return listeners.addListener(listener);
    }

    private void record(ListChange<E> change, ChangeOrigin origin) {
        if (batchDepth > 0) {
            batchedChanges.add(change);
            return;
        }
        fire(ListChangeSet.of(change, origin));
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
