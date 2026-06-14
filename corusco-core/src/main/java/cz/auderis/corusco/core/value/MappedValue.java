package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;
import java.util.ArrayList;
import java.util.List;
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
 * <p>The instance owns the source subscription and its own listeners. Closing
 * the mapped value removes the source subscription, clears listeners, and makes
 * later subscriptions no-ops. Null source values are passed to the mapper, so
 * the mapper owns null-handling policy.</p>
 *
 * <p>The class is Swing-free, not synchronized, and inherits the threading
 * assumptions of the source value.</p>
 *
 * @param <A> source value type
 * @param <B> mapped value type
 */
public final class MappedValue<A, B> implements ReadableValue<B>, Disposable {

    private final ReadableValue<A> source;
    private final Function<? super A, ? extends B> mapper;
    private final Subscription sourceSubscription;
    private final List<ValueChangeListener<B>> listeners = new ArrayList<>();
    private B value;
    private boolean closed;

    /**
     * Creates a mapped value.
     *
     * @param source source value
     * @param mapper mapping function, invoked synchronously
     * @param <A> source value type
     * @param <B> mapped value type
     * @return mapped value subscribed to the source
     */
    public static <A, B> MappedValue<A, B> of(
            ReadableValue<A> source,
            Function<? super A, ? extends B> mapper
    ) {
        return new MappedValue<>(source, mapper);
    }

    private MappedValue(ReadableValue<A> source, Function<? super A, ? extends B> mapper) {
        this.source = Objects.requireNonNull(source, "source");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.value = mapper.apply(source.value());
        this.sourceSubscription = source.subscribe(event -> recompute(event.origin()));
    }

    @Override
    public B value() {
        return value;
    }

    @Override
    public Subscription subscribe(ValueChangeListener<B> listener) {
        Objects.requireNonNull(listener, "listener");
        if (closed) {
            return Subscription.EMPTY;
        }
        listeners.add(listener);
        return Subscription.of(() -> listeners.remove(listener));
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
            listeners.clear();
        }
    }

    private void recompute(ChangeOrigin origin) {
        if (closed) {
            return;
        }
        B oldValue = value;
        B newValue = mapper.apply(source.value());
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        value = newValue;
        ValueChangeEvent<B> event = new ValueChangeEvent<>(this, oldValue, newValue, origin);
        List<ValueChangeListener<B>> snapshot = List.copyOf(listeners);
        for (ValueChangeListener<B> listener : snapshot) {
            listener.valueChanged(event);
        }
    }
}
