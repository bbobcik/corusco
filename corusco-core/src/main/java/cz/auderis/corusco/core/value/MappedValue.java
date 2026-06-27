package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * A one-way derived value computed by mapping a source value.
 *
 * <p>Use this class for the common one-source derived-value case, for example
 * converting a selected row into a display string or mapping a task state to a
 * boolean. The mapped value subscribes to its source, computes an initial value
 * immediately, updates synchronously when the source changes, and emits an
 * event only when the mapped value changes.</p>
 *
 * <p>Events emitted by this value propagate the origin of the source event that
 * caused recomputation. Initial computation in the factory path does not emit
 * an event. Propagating the source origin lets listeners distinguish a mapped
 * value changed by a direct user edit, model load, binding echo, or custom
 * origin without losing the causal classification at the projection boundary.</p>
 *
 * <p>The instance owns the source subscription and its own listeners. Closing
 * the mapped value removes the source subscription, clears listeners, and makes
 * later subscriptions no-ops. Null source values are passed to the mapper, and
 * mapper results may also be {@code null}; the mapper owns the null-handling
 * policy.</p>
 *
 * <p>The class is Swing-free, not synchronized, and inherits the threading
 * assumptions of the source value.</p>
 *
 * @param <A> source value type; source values passed to the mapper may be
 *         {@code null}
 * @param <B> mapped value type; mapper results may be {@code null}
 */
public final class MappedValue<A, B> implements ReadableValue<B>, Disposable {

    private final ReadableValue<A> source;
    private final Function<? super @Nullable A, ? extends @Nullable B> mapper;
    private final Subscription sourceSubscription;
    private final ListenerSet<ValueChangeListener<B>, ValueChangeEvent<B>> listeners = new ListenerSet<>();
    private @Nullable B value;
    private boolean closed;

    /**
     * Creates a mapped value.
     *
     * @param source source value
     * @param mapper mapping function, invoked synchronously; receives nullable
     *         source values and may return {@code null}
     * @param <A> source value type
     * @param <B> mapped value type
     * @return mapped value subscribed to the source
     */
    public static <A, B> MappedValue<A, B> of(
            ReadableValue<A> source,
            Function<? super @Nullable A, ? extends @Nullable B> mapper
    ) {
        return new MappedValue<>(source, mapper);
    }

    private MappedValue(ReadableValue<A> source, Function<? super @Nullable A, ? extends @Nullable B> mapper) {
        this.source = Objects.requireNonNull(source, "source");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.value = mapper.apply(source.value());
        this.sourceSubscription = source.subscribe(event -> recompute(event.origin()));
    }

    @Override
    public @Nullable B value() {
        return value;
    }

    @Override
    public Subscription subscribe(ValueChangeListener<B> listener) {
        Objects.requireNonNull(listener, "listener");
        if (closed) {
            return Subscription.EMPTY;
        }
        return listeners.addListener(listener);
    }

    /**
     * Closes the source subscription and removes mapped value listeners.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            sourceSubscription.close();
        } finally {
            listeners.clearListeners();
        }
    }

    private void recompute(ChangeOrigin origin) {
        if (closed) {
            return;
        }
        @Nullable B oldValue = value;
        @Nullable B newValue = mapper.apply(source.value());
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        value = newValue;
        ValueChangeEvent<B> event = new ValueChangeEvent<>(this, oldValue, newValue, origin);
        listeners.fireEvent(event, ValueChangeListener::valueChanged);
    }
}
