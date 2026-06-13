package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;
import cz.auderis.corusco.core.lifecycle.SubscriptionScope;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A one-way value derived from one or more source values.
 *
 * <p>The derived value recomputes synchronously when any dependency changes and
 * emits one event when the computed value changes. Closing the derived value
 * removes its dependency subscriptions and its own listeners. The class is not
 * synchronized and inherits the threading assumptions of its dependencies.</p>
 *
 * @param <T> derived value type
 */
public final class DerivedValue<T> implements ReadableValue<T>, Disposable {

    private final Supplier<? extends T> supplier;
    private final SubscriptionScope dependencyScope = new SubscriptionScope();
    private final List<ValueChangeListener<T>> listeners = new ArrayList<>();
    private T value;
    private boolean closed;

    private DerivedValue(Supplier<? extends T> supplier, Iterable<? extends ReadableValue<?>> dependencies) {
        this.supplier = Objects.requireNonNull(supplier, "supplier");
        Objects.requireNonNull(dependencies, "dependencies");
        this.value = supplier.get();
        for (ReadableValue<?> dependency : dependencies) {
            Objects.requireNonNull(dependency, "dependency");
            dependencyScope.add(dependency.subscribe(event -> recompute(event.origin())));
        }
    }

    /**
     * Creates a derived value from a supplier and dependencies.
     *
     * @param supplier computes the current derived value, possibly {@code null}
     * @param dependencies values that trigger recomputation
     * @param <T> derived value type
     * @return a derived value subscribed to the dependencies
     */
    public static <T> DerivedValue<T> of(
            Supplier<? extends T> supplier,
            Iterable<? extends ReadableValue<?>> dependencies
    ) {
        return new DerivedValue<>(supplier, dependencies);
    }

    /**
     * Creates a derived value from a supplier and dependencies.
     *
     * @param supplier computes the current derived value, possibly {@code null}
     * @param dependencies values that trigger recomputation
     * @param <T> derived value type
     * @return a derived value subscribed to the dependencies
     */
    public static <T> DerivedValue<T> of(
            Supplier<? extends T> supplier,
            ReadableValue<?>... dependencies
    ) {
        return new DerivedValue<>(supplier, List.of(dependencies));
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public Subscription subscribe(ValueChangeListener<T> listener) {
        Objects.requireNonNull(listener, "listener");
        if (closed) {
            return Subscription.EMPTY;
        }
        listeners.add(listener);
        return Subscription.of(() -> listeners.remove(listener));
    }

    /**
     * Closes dependency subscriptions and removes listeners.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            dependencyScope.close();
        } finally {
            listeners.clear();
        }
    }

    private void recompute(ChangeOrigin origin) {
        if (closed) {
            return;
        }
        T oldValue = value;
        T newValue = supplier.get();
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        value = newValue;
        ValueChangeEvent<T> event = new ValueChangeEvent<>(this, oldValue, newValue, origin);
        List<ValueChangeListener<T>> snapshot = List.copyOf(listeners);
        for (ValueChangeListener<T> listener : snapshot) {
            listener.valueChanged(event);
        }
    }
}
