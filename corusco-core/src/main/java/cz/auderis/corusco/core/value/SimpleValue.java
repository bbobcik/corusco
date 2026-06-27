package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Basic mutable {@link WritableValue} implementation.
 *
 * <p>This is the default scalar observable used throughout Corusco models for
 * field values, dirty flags, command state, busy state, and simple presenter
 * state. It owns the current value and the listener registrations returned from
 * {@link #subscribe(ValueChangeListener)}.</p>
 *
 * <p>Mutation and listener dispatch are synchronous. This class is not
 * synchronized and is intended for single-owner presentation state. Equal
 * values, including equal {@code null} values, do not emit events. Listener
 * dispatch uses a snapshot so removing a listener during dispatch does not skip
 * or duplicate unrelated listeners.</p>
 *
 * <p>When a mutation emits an event, the event origin is exactly the origin
 * supplied to {@link #setValue(Object, ChangeOrigin)}. The convenience
 * {@link #setValue(Object)} method inherited from {@link WritableValue} uses
 * {@link StandardChangeOrigin#MODEL}. This class does not reinterpret or
 * replace caller-provided origins.</p>
 *
 * <p>The value does not know about Swing or background tasks. If it is bound to
 * a Swing component, the binding or presenter is responsible for EDT
 * confinement.</p>
 *
 * @param <T> value type; the stored value may be {@code null}
 */
public final class SimpleValue<T> implements WritableValue<T> {

    private final ListenerSet<ValueChangeListener<T>, ValueChangeEvent<T>> listeners = new ListenerSet<>();
    private @Nullable T value;

    /**
     * Creates a value with an initial value.
     *
     * @param initialValue initial value, possibly {@code null}
     */
    public SimpleValue(@Nullable T initialValue) {
        this.value = initialValue;
    }

    /**
     * Creates a value with a {@code null} initial value.
     *
     * @param <T> value type
     * @return a value initialized to {@code null}
     */
    public static <T> SimpleValue<T> empty() {
        return new SimpleValue<>(null);
    }

    /**
     * Creates a value with an initial value.
     *
     * @param initialValue initial value, possibly {@code null}
     * @param <T> value type
     * @return a value initialized to {@code initialValue}
     */
    public static <T> SimpleValue<T> of(@Nullable T initialValue) {
        return new SimpleValue<>(initialValue);
    }

    @Override
    public @Nullable T value() {
        return value;
    }

    @Override
    public void setValue(@Nullable T newValue, ChangeOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        @Nullable T oldValue = value;
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        value = newValue;
        fireChanged(oldValue, newValue, origin);
    }

    @Override
    public Subscription subscribe(ValueChangeListener<T> listener) {
        return listeners.addListener(listener);
    }

    /**
     * Emits an already accepted value change to current listeners.
     *
     * @param oldValue previous value
     * @param newValue new value
     * @param origin change origin
     */
    private void fireChanged(@Nullable T oldValue, @Nullable T newValue, ChangeOrigin origin) {
        ValueChangeEvent<T> event = new ValueChangeEvent<>(this, oldValue, newValue, origin);
        listeners.fireEvent(event, ValueChangeListener::valueChanged);
    }
}
