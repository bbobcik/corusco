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
import java.util.function.Function;

/**
 * Observable scalar view of the first collection element matching a selected key.
 *
 * <p>Use this bridge when selection is stored as a stable key instead of a row
 * index. Typical callers keep an identifier in a {@link ReadableValue} and
 * expose the currently matching collection item to forms, detail panes, or
 * command state. The source collection remains the owner of item ordering and
 * contents; this class performs a read-only lookup over each current snapshot.</p>
 *
 * <p>The value is computed immediately from the current source snapshot and
 * selected key, without emitting an initial event. A {@code null} selected key
 * represents absence and makes {@link #value()} return {@code null}. If several
 * elements have equal extracted keys, the first match in snapshot order is
 * selected.</p>
 *
 * <p>Events preserve the origin of the dependency that caused recomputation:
 * collection changes use {@link ListChangeSet#origin()} and key changes use
 * {@link ValueChangeEvent#origin()}. Equal effective values, including repeated
 * absence, do not emit events. Extracted keys may be {@code null}; they simply
 * cannot match a {@code null} selected key because {@code null} selected keys
 * are reserved for no selection.</p>
 *
 * <p>The instance owns its source and key subscriptions. Closing it removes
 * those subscriptions, clears listeners, and makes later subscriptions return
 * {@link Subscription#EMPTY}. The class is not synchronized and inherits the
 * threading assumptions of its source collection and key value.</p>
 *
 * @param <E> non-null source element type; {@link #value()} may still be
 *        {@code null} to represent absence
 * @param <K> selected-key type
 */
public final class CollectionMatchingValue<E extends @NonNull Object, K> implements ReadableValue<E>, Disposable {

    private final ObservableReadableCollection<E> source;
    private final ReadableValue<K> key;
    private final Function<? super E, ? extends @Nullable K> keyExtractor;
    private final Subscription sourceSubscription;
    private final Subscription keySubscription;
    private final ListenerSet<ValueChangeListener<E>, ValueChangeEvent<E>> listeners = new ListenerSet<>();
    private @Nullable E value;
    private boolean closed;

    /**
     * Creates a matching item value and subscribes it to both dependencies.
     *
     * @param source source collection searched in snapshot order
     * @param key selected key value; a {@code null} value means no selected item
     * @param keyExtractor function that extracts comparable keys from elements,
     *        possibly {@code null}
     */
    public CollectionMatchingValue(
            ObservableReadableCollection<E> source,
            ReadableValue<K> key,
            Function<? super E, ? extends @Nullable K> keyExtractor
    ) {
        this.source = Objects.requireNonNull(source, "source");
        this.key = Objects.requireNonNull(key, "key");
        this.keyExtractor = Objects.requireNonNull(keyExtractor, "keyExtractor");
        this.value = compute();
        this.sourceSubscription = source.subscribe(changes -> recompute(changes.origin()));
        this.keySubscription = key.subscribe(event -> recompute(event.origin()));
    }

    /**
     * Creates a matching item value and subscribes it to both dependencies.
     *
     * @param source source collection searched in snapshot order
     * @param key selected key value; a {@code null} value means no selected item
     * @param keyExtractor function that extracts comparable keys from elements,
     *        possibly {@code null}
     * @param <E> element type
     * @param <K> key type
     * @return matching item value backed by the supplied collection and key
     */
    public static <E extends @NonNull Object, K> CollectionMatchingValue<E, K> of(
            ObservableReadableCollection<E> source,
            ReadableValue<K> key,
            Function<? super E, ? extends @Nullable K> keyExtractor
    ) {
        return new CollectionMatchingValue<>(source, key, keyExtractor);
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
                keySubscription.close();
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
        @Nullable K selectedKey = key.value();
        if (selectedKey == null) {
            return null;
        }
        return source.stream()
                .filter(element -> Objects.equals(selectedKey, keyExtractor.apply(element)))
                .findFirst()
                .orElse(null);
    }
}
