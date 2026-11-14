package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Basic mutable {@link WritableValue} implementation.
 *
 * <p>This is the default scalar observable used throughout Corusco models for
 * field values, dirty flags, command state, busy state, and simple presenter
 * state. It owns the current value and the listener registrations returned from
 * {@link #subscribe(ValueChangeListener)}.</p>
 *
 * <p>Ordinary instances mutate and dispatch synchronously. A backed value may
 * use its owner's dispatcher to defer a reentrant mutation. This class is not
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
    private final @Nullable Supplier<@Nullable T> reader;
    private final @Nullable Consumer<@Nullable T> writer;
    private final Consumer<Runnable> mutationDispatcher;

    /**
     * Creates a value with an initial value.
     *
     * @param initialValue initial value, possibly {@code null}
     */
    public SimpleValue(@Nullable T initialValue) {
        this.value = initialValue;
        this.reader = null;
        this.writer = null;
        this.mutationDispatcher = Runnable::run;
    }

    private SimpleValue(Supplier<@Nullable T> reader, Consumer<@Nullable T> writer, Consumer<Runnable> mutationDispatcher) {
        this.reader = Objects.requireNonNull(reader, "reader");
        this.writer = Objects.requireNonNull(writer, "writer");
        this.mutationDispatcher = Objects.requireNonNull(mutationDispatcher, "mutationDispatcher");
    }

    /** Creates an owner capability and its value backed by externally owned state. */
    public static <T> Backing<T> backedBy(Supplier<@Nullable T> reader, Consumer<@Nullable T> writer) {
        return backedBy(reader, writer, Runnable::run);
    }

    /** Creates a backed value whose owner serializes mutations and their notifications, including null values. */
    public static <T> Backing<T> backedBy(Supplier<@Nullable T> reader, Consumer<@Nullable T> writer, Consumer<Runnable> mutationDispatcher) {
        return new Backing<>(new SimpleValue<>(reader, writer, mutationDispatcher));
    }

    /** Owner-only capability for announcing a change already installed in backing state. */
    public static final class Backing<T> {
        private final SimpleValue<T> value;
        private Backing(SimpleValue<T> value) { this.value = value; }
        public SimpleValue<T> value() { return value; }
        public void notifyChange(@Nullable T oldValue, @Nullable T newValue, ChangeOrigin origin) {
            Objects.requireNonNull(origin, "origin");
            if (!Objects.equals(oldValue, newValue)) value.fireChanged(oldValue, newValue, origin);
        }
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
        return reader == null ? value : reader.get();
    }

    @Override
    public void setValue(@Nullable T newValue, ChangeOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        mutationDispatcher.accept(() -> setValueNow(newValue, origin));
    }

    private void setValueNow(@Nullable T newValue, ChangeOrigin origin) {
        @Nullable T oldValue = value();
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        if (writer == null) {
            value = newValue;
        } else {
            writer.accept(newValue);
        }
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
