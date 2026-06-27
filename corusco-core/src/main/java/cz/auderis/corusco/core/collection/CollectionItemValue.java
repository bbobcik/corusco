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

import java.util.Objects;

/**
 * Observable scalar view of the element at an observable collection index.
 *
 * <p>Use this bridge for presentation state where a list-like model and a
 * selected row index should be exposed as one scalar value, for example the
 * currently selected table row. The source collection remains the owner of its
 * contents and the index value remains the owner of selection state; this class
 * only joins the two read models.</p>
 *
 * <p>The value is computed immediately from the current source snapshot and
 * current index, without emitting an initial event. Later source changes and
 * index changes recompute the effective item synchronously. A {@code null},
 * negative, or out-of-range index represents absence and makes {@link #value()}
 * return {@code null}.</p>
 *
 * <p>Events preserve the origin of the dependency that caused recomputation:
 * collection changes use {@link ListChangeSet#origin()} and index changes use
 * {@link ValueChangeEvent#origin()}. Equal effective values, including repeated
 * absence, do not emit events.</p>
 *
 * <p>The instance owns its source and index subscriptions. Closing it removes
 * those subscriptions, clears listeners, and makes later subscriptions return
 * {@link Subscription#EMPTY}. The class is not synchronized and inherits the
 * threading assumptions of its source collection and index value.</p>
 *
 * @param <E> non-null source element type; {@link #value()} may still be
 *        {@code null} to represent absence
 */
public final class CollectionItemValue<E extends @NonNull Object> implements ReadableValue<E>, Disposable {

    private final ObservableReadableCollection<E> source;
    private final ReadableValue<Integer> index;
    private final Subscription sourceSubscription;
    private final Subscription indexSubscription;
    private final ListenerSet<ValueChangeListener<E>, ValueChangeEvent<E>> listeners = new ListenerSet<>();
    private @Nullable E value;
    private boolean closed;

    /**
     * Creates an item value and subscribes it to both dependencies.
     *
     * @param source source collection that supplies ordered elements
     * @param index observable index; a {@code null} value means no selected
     *        item
     */
    public CollectionItemValue(ObservableReadableCollection<E> source, ReadableValue<Integer> index) {
        this.source = Objects.requireNonNull(source, "source");
        this.index = Objects.requireNonNull(index, "index");
        this.value = compute();
        this.sourceSubscription = source.subscribe(changes -> recompute(changes.origin()));
        this.indexSubscription = index.subscribe(event -> recompute(event.origin()));
    }

    /**
     * Creates an item value and subscribes it to both dependencies.
     *
     * @param source source collection that supplies ordered elements
     * @param index observable index; a {@code null} value means no selected
     *        item
     * @param <E> element type
     * @return item value backed by the supplied collection and index
     */
    public static <E extends @NonNull Object> CollectionItemValue<E> of(
            ObservableReadableCollection<E> source,
            ReadableValue<Integer> index
    ) {
        return new CollectionItemValue<>(source, index);
    }

    @Override
    public @Nullable E value() {
        return value;
    }

    @Override
    public Subscription subscribe(ValueChangeListener<E> listener) {
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
            sourceSubscription.close();
        } finally {
            try {
                indexSubscription.close();
            } finally {
                listeners.clearListeners();
            }
        }
    }

    private void recompute(ChangeOrigin origin) {
        if (closed) {
            return;
        }
        @Nullable E oldValue = value;
        @Nullable E newValue = compute();
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        value = newValue;
        ValueChangeEvent<E> event = new ValueChangeEvent<>(this, oldValue, newValue, origin);
        listeners.fireEvent(event, ValueChangeListener::valueChanged);
    }

    private @Nullable E compute() {
        Integer currentIndex = index.value();
        if (currentIndex == null || currentIndex < 0 || currentIndex >= source.size()) {
            return null;
        }
        return source.get(currentIndex);
    }
}
