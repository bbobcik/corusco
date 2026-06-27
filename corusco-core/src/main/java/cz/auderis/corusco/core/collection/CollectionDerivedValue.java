package cz.auderis.corusco.core.collection;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.ValueChangeEvent;
import cz.auderis.corusco.core.value.ValueChangeListener;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Observable scalar value derived from an observable collection.
 *
 * <p>Use this bridge when scalar presentation state is a function of all current
 * collection contents, for example row count, empty state, a copied snapshot,
 * an aggregate label, or a validation summary. It is the collection counterpart
 * of {@link cz.auderis.corusco.core.value.DerivedValue}: the collection is the
 * dependency, and the mapper turns current collection contents into one scalar
 * result.</p>
 *
 * <p>The regular {@link #of(ObservableReadableCollection, Function)} factory
 * gives the mapper an immutable ordered snapshot from
 * {@link ObservableReadableCollection#snapshot()}. Stream-based factories such
 * as {@link #fromStream(ObservableReadableCollection, Function)} and
 * {@link #anyMatch(ObservableReadableCollection, Predicate)} use
 * {@link ObservableReadableCollection#stream()} instead, avoiding snapshot
 * allocation when the derived calculation only needs immediate traversal.</p>
 *
 * <p>The initial value is computed immediately, without emitting an initial
 * event. Later source changes recompute synchronously and use
 * {@link ListChangeSet#origin()} as the emitted value event origin.</p>
 *
 * <p>Mappers may return {@code null}. Equal recomputed values, including
 * repeated {@code null}, do not emit events. Snapshot mappers may retain the
 * immutable list instance they receive. Stream mappers must consume the supplied
 * stream inside the mapper call and must not retain it.</p>
 *
 * <p>The instance owns its source subscription. Closing it removes that
 * subscription, clears listeners, and makes later subscriptions return
 * {@link Subscription#EMPTY}. The class is not synchronized and inherits the
 * threading assumptions of its source collection.</p>
 *
 * @param <E> non-null source element type
 * @param <T> derived value type; mapper results may be {@code null}
 */
public final class CollectionDerivedValue<E extends @NonNull Object, T> implements ReadableValue<T>, Disposable {

    private final Supplier<? extends @Nullable T> supplier;
    private final Subscription subscription;
    private final ListenerSet<ValueChangeListener<T>, ValueChangeEvent<T>> listeners = new ListenerSet<>();
    private @Nullable T value;
    private boolean closed;

    /**
     * Creates a derived collection value and subscribes it to the source.
     *
     * @param source source collection supplying immutable ordered snapshots
     * @param mapper snapshot mapper; may return {@code null}
     */
    public CollectionDerivedValue(
            ObservableReadableCollection<E> source,
            Function<List<E>, ? extends @Nullable T> mapper
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(mapper, "mapper");
        this.supplier = () -> mapper.apply(source.snapshot());
        this.value = compute();
        this.subscription = source.subscribe(changes -> recompute(changes.origin()));
    }

    private CollectionDerivedValue(
            ObservableReadableCollection<E> source,
            Supplier<? extends @Nullable T> supplier
    ) {
        Objects.requireNonNull(source, "source");
        this.supplier = Objects.requireNonNull(supplier, "supplier");
        this.value = compute();
        this.subscription = source.subscribe(changes -> recompute(changes.origin()));
    }

    /**
     * Creates a derived collection value and subscribes it to the source.
     *
     * @param source source collection supplying immutable ordered snapshots
     * @param mapper snapshot mapper; may return {@code null}
     * @param <E> source element type
     * @param <T> derived value type
     * @return derived value backed by the supplied collection and mapper
     */
    public static <E extends @NonNull Object, T> CollectionDerivedValue<E, T> of(
            ObservableReadableCollection<E> source,
            Function<List<E>, ? extends @Nullable T> mapper
    ) {
        return new CollectionDerivedValue<>(source, mapper);
    }

    /**
     * Creates a derived collection value from an immediate source stream.
     *
     * <p>The mapper receives a fresh stream for each recomputation. The stream
     * has the same non-snapshot contract as {@link ObservableReadableCollection#stream()}:
     * it is intended for immediate traversal and must not be retained by the
     * mapper.</p>
     *
     * @param source source collection supplying current ordered streams
     * @param mapper stream mapper; may return {@code null}
     * @param <E> source element type
     * @param <T> derived value type
     * @return derived value backed by the supplied collection and stream mapper
     */
    public static <E extends @NonNull Object, T> CollectionDerivedValue<E, T> fromStream(
            ObservableReadableCollection<E> source,
            Function<Stream<E>, ? extends @Nullable T> mapper
    ) {
        Objects.requireNonNull(mapper, "mapper");
        return new CollectionDerivedValue<>(source, () -> mapper.apply(source.stream()));
    }

    /**
     * Creates a value containing the current source collection size.
     *
     * <p>The value updates when a source change changes the effective size and
     * uses the source change origin for emitted events.</p>
     *
     * @param source source collection
     * @param <E> source element type
     * @return size value
     */
    public static <E extends @NonNull Object> CollectionDerivedValue<E, Integer> size(
            ObservableReadableCollection<E> source
    ) {
        return of(source, List::size);
    }

    /**
     * Creates a value containing the current source collection stream count.
     *
     * <p>This is a stream-based alternative to {@link #size(ObservableReadableCollection)}
     * for callers that want a {@code long} count and accept the immediate
     * traversal contract of {@link ObservableReadableCollection#stream()}.</p>
     *
     * @param source source collection
     * @param <E> source element type
     * @return count value
     */
    public static <E extends @NonNull Object> CollectionDerivedValue<E, Long> count(
            ObservableReadableCollection<E> source
    ) {
        return fromStream(source, Stream::count);
    }

    /**
     * Creates a value indicating whether the source collection is empty.
     *
     * <p>The value updates only when source changes cross the empty/non-empty
     * boundary and uses the source change origin for emitted events.</p>
     *
     * @param source source collection
     * @param <E> source element type
     * @return empty-state value
     */
    public static <E extends @NonNull Object> CollectionDerivedValue<E, Boolean> isEmpty(
            ObservableReadableCollection<E> source
    ) {
        return of(source, List::isEmpty);
    }

    /**
     * Creates a value indicating whether any source element matches a predicate.
     *
     * @param source source collection
     * @param predicate predicate tested against current source elements
     * @param <E> source element type
     * @return predicate-match value
     */
    public static <E extends @NonNull Object> CollectionDerivedValue<E, Boolean> anyMatch(
            ObservableReadableCollection<E> source,
            Predicate<? super E> predicate
    ) {
        Objects.requireNonNull(predicate, "predicate");
        return fromStream(source, stream -> stream.anyMatch(predicate));
    }

    /**
     * Creates a value indicating whether all source elements match a predicate.
     *
     * @param source source collection
     * @param predicate predicate tested against current source elements
     * @param <E> source element type
     * @return all-match value
     */
    public static <E extends @NonNull Object> CollectionDerivedValue<E, Boolean> allMatch(
            ObservableReadableCollection<E> source,
            Predicate<? super E> predicate
    ) {
        Objects.requireNonNull(predicate, "predicate");
        return fromStream(source, stream -> stream.allMatch(predicate));
    }

    /**
     * Creates a value indicating whether no source element matches a predicate.
     *
     * @param source source collection
     * @param predicate predicate tested against current source elements
     * @param <E> source element type
     * @return no-match value
     */
    public static <E extends @NonNull Object> CollectionDerivedValue<E, Boolean> noneMatch(
            ObservableReadableCollection<E> source,
            Predicate<? super E> predicate
    ) {
        Objects.requireNonNull(predicate, "predicate");
        return fromStream(source, stream -> stream.noneMatch(predicate));
    }


    /**
     * Creates a value containing the current immutable source snapshot.
     *
     * <p>Each source change that changes the ordered contents produces the new
     * snapshot value with the source change origin.</p>
     *
     * @param source source collection
     * @param <E> source element type
     * @return snapshot value
     */
    public static <E extends @NonNull Object> CollectionDerivedValue<E, List<E>> snapshot(
            ObservableReadableCollection<E> source
    ) {
        return of(source, snapshot -> snapshot);
    }

    @Override
    public @Nullable T value() {
        return value;
    }

    @Override
    public Subscription subscribe(ValueChangeListener<T> listener) {
        Objects.requireNonNull(listener, "listener");
        if (closed) {
            return Subscription.EMPTY;
        }
        return listeners.addListener(listener);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            subscription.close();
        } finally {
            listeners.clearListeners();
        }
    }

    private void recompute(ChangeOrigin origin) {
        if (closed) {
            return;
        }
        @Nullable T oldValue = value;
        @Nullable T newValue = compute();
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        value = newValue;
        ValueChangeEvent<T> event = new ValueChangeEvent<>(this, oldValue, newValue, origin);
        listeners.fireEvent(event, ValueChangeListener::valueChanged);
    }

    private @Nullable T compute() {
        return supplier.get();
    }
}
