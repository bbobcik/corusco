package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/**
 * Mutable observable sorted set with ordered indexed reads.
 *
 * <p>The set owns unique sorted storage. Uniqueness follows the comparator:
 * when two elements compare as equal, they represent the same set entry even if
 * {@link Object#equals(Object)} would distinguish them. Snapshots and emitted
 * list-change indices use the current sorted iteration order.</p>
 *
 * <p>This type intentionally does not implement {@link ObservableList}.
 * Indexed insertion, replacement, and move operations do not have honest set
 * semantics. Consumers that only need ordered read access should use this set
 * through {@link ObservableReadableCollection}. Use {@link #asList()} only for
 * compatibility with APIs that still require an {@link ObservableList}.</p>
 *
 * @param <E> element type
 */
public final class ObservableSortedSet<E> implements ObservableReadableCollection<E> {

    private final TreeSet<E> elements;
    private final ListenerSet<ListChangeListener<E>, ListChangeSet<E>> listeners;
    private final ObservableList<E> listView;

    /**
     * Creates an empty observable sorted set.
     *
     * @param comparator sort and uniqueness comparator
     */
    public ObservableSortedSet(Comparator<? super E> comparator) {
        Objects.requireNonNull(comparator, "comparator");
        this.elements = new TreeSet<>(comparator);
        this.listeners = new ListenerSet<>();
        this.listView = new SortedSetListView();
    }

    /**
     * Creates an observable sorted set with initial contents.
     *
     * <p>The supplied collection is copied during construction. Duplicate
     * values according to the comparator are collapsed, and construction itself
     * does not fire change events.</p>
     *
     * @param initialElements initial elements
     * @param comparator sort and uniqueness comparator
     */
    public ObservableSortedSet(Collection<? extends E> initialElements, Comparator<? super E> comparator) {
        this(comparator);
        Objects.requireNonNull(initialElements, "initialElements");
        elements.addAll(List.copyOf(initialElements));
    }

    /**
     * Creates an empty observable sorted set.
     *
     * @param comparator sort and uniqueness comparator
     * @param <E> element type
     * @return observable sorted set
     */
    public static <E> ObservableSortedSet<E> empty(Comparator<? super E> comparator) {
        return new ObservableSortedSet<>(comparator);
    }

    /**
     * Creates an observable sorted set with initial contents.
     *
     * @param elements initial elements, copied into the new set
     * @param comparator sort and uniqueness comparator
     * @param <E> element type
     * @return observable sorted set
     */
    public static <E> ObservableSortedSet<E> of(
            Collection<? extends E> elements,
            Comparator<? super E> comparator
    ) {
        return new ObservableSortedSet<>(elements, comparator);
    }

    @Override
    public int size() {
        return elements.size();
    }

    /**
     * Returns whether this set contains an element equivalent to the supplied
     * value according to the comparator.
     *
     * @param element element to test
     * @return {@code true} if present
     */
    public boolean contains(E element) {
        Objects.requireNonNull(element, "element");
        return elements.contains(element);
    }

    /**
     * Adds an element when no comparator-equivalent element is present.
     *
     * @param element element to add
     * @return {@code true} when the set changed
     */
    public boolean add(E element) {
        Objects.requireNonNull(element, "element");
        if (!elements.add(element)) {
            return false;
        }
        int index = indexOfEquivalent(element);
        final var event = new ListChange.Inserted<>(index, singleton(element));
        fireEvent(event);
        return true;
    }

    /**
     * Removes an element equivalent to the supplied value.
     *
     * @param element element to remove
     * @return {@code true} when the set changed
     */
    public boolean remove(E element) {
        Objects.requireNonNull(element, "element");
        LocatedElement<E> existing = findEquivalent(element);
        if (existing == null || !elements.remove(existing.element())) {
            return false;
        }
        final ListChange.Removed<E> event = new ListChange.Removed<>(existing.index(), singleton(existing.element()));
        fireEvent(event);
        return true;
    }

    /**
     * Clears the set.
     */
    public void clear() {
        if (elements.isEmpty()) {
            return;
        }
        List<E> removed = snapshot();
        elements.clear();
        fire(ListChangeSet.of(new ListChange.Cleared<>(removed)));
    }

    /**
     * Returns the comparator used for sorting and uniqueness.
     *
     * @return comparator
     */
    public Comparator<? super E> comparator() {
        final Comparator<? super E> cmp = elements.comparator();
        if (null == cmp) {
            throw new IllegalStateException("comparator is null");
        }
        return cmp;
    }

    @Override
    public List<E> snapshot() {
        return List.copyOf(elements);
    }

    /**
     * Returns a read-only observable list view over this set.
     *
     * <p>The returned view is a compatibility adapter for list-oriented APIs.
     * It delegates reads and subscriptions to this set, and all mutation
     * methods throw {@link UnsupportedOperationException}. Prefer using this
     * set directly where an {@link ObservableReadableCollection} is accepted.</p>
     *
     * @return list view
     */
    public ObservableList<E> asList() {
        return listView;
    }

    @Override
    public Subscription subscribe(ListChangeListener<E> listener) {
        return listeners.addListener(listener);
    }

    private void fireEvent(ListChange<E> event) {
        final ListChangeSet<E> changeSet = ListChangeSet.of(event);
        listeners.fireEvent(changeSet, ListChangeListener::listChanged);
    }

    private void fire(ListChangeSet<E> changeSet) {
        listeners.fireEvent(changeSet, ListChangeListener::listChanged);
    }

    @Override
    public E get(int index) {
        if (index < 0 || index >= elements.size()) {
            throw new IndexOutOfBoundsException(index);
        }
        int current = 0;
        for (E element : elements) {
            if (current == index) {
                return element;
            }
            current++;
        }
        throw new IndexOutOfBoundsException(index);
    }

    private @Nullable LocatedElement<E> findEquivalent(E element) {
        final Comparator<? super E> comparator = elements.comparator();
        assert null != comparator : "comparator is null";
        E existing = elements.ceiling(element);
        if (existing == null || comparator.compare(existing, element) != 0) {
            return null;
        }
        return new LocatedElement<>(indexOf(existing), existing);
    }

    private int indexOfEquivalent(E element) {
        Comparator<? super E> comparator = elements.comparator();
        assert null != comparator : "comparator is null";
        E existing = elements.ceiling(element);
        if (existing == null || comparator.compare(existing, element) != 0) {
            throw new IllegalStateException("element is not present");
        }
        return indexOf(existing);
    }

    private int indexOf(E element) {
        return elements.headSet(element, false).size();
    }

    private List<E> singleton(E element) {
        List<E> result = new ArrayList<>(1);
        result.add(element);
        return result;
    }

    private UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException("ObservableSortedSet list view is read-only; mutate the set");
    }

    private record LocatedElement<E>(int index, E element) {
    }

    private final class SortedSetListView implements ObservableList<E> {

        @Override
        public int size() {
            return ObservableSortedSet.this.size();
        }

        @Override
        public E get(int index) {
            return ObservableSortedSet.this.get(index);
        }

        @Override
        public List<E> snapshot() {
            return ObservableSortedSet.this.snapshot();
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
            return ObservableSortedSet.this.subscribe(listener);
        }
    }
}
