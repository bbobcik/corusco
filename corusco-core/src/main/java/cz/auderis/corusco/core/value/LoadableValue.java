package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Supplier-backed detachable value that loads lazily and caches while attached.
 *
 * <p>Use this class when a presenter exposes a value that can be loaded on
 * demand and released between view activations. The first call to
 * {@link #value()} after construction or detach invokes the loader and marks
 * the cache attached. {@link #detach()} and {@link #invalidate()} release the
 * cached value without publishing a value-change event. {@link #refresh()}
 * reloads explicitly and notifies subscribers if the effective value changes.</p>
 *
 * <p>The loader is invoked on the caller's thread. This class is intentionally
 * not synchronized; it follows the value package convention of single-owner
 * presentation state. The loaded value may be {@code null}; cache attachment is
 * tracked separately from the cached object.</p>
 *
 * <p>The instance retains subscribers until their subscriptions are closed. It
 * does not own external resources used by the loader.</p>
 *
 * @param <T> value type
 */
public final class LoadableValue<T> implements DetachableValue<T> {

    private final Supplier<? extends T> loader;
    private final List<ValueChangeListener<T>> listeners = new ArrayList<>();
    private boolean attached;
    private T value;

    /**
     * Creates a lazy value backed by a loader.
     *
     * @param loader loader invoked when the value is first accessed after
     *         construction or detach
     */
    public LoadableValue(Supplier<? extends T> loader) {
        this.loader = Objects.requireNonNull(loader, "loader");
    }

    /**
     * Creates a lazy value backed by a loader.
     *
     * @param loader loader invoked when the value is first accessed after
     *         construction or detach
     * @param <T> value type
     * @return loadable value
     */
    public static <T> LoadableValue<T> of(Supplier<? extends T> loader) {
        return new LoadableValue<>(loader);
    }

    @Override
    public T value() {
        if (!attached) {
            value = loader.get();
            attached = true;
        }
        return value;
    }

    @Override
    public Subscription subscribe(ValueChangeListener<T> listener) {
        Objects.requireNonNull(listener, "listener");
        listeners.add(listener);
        return Subscription.of(() -> listeners.remove(listener));
    }

    @Override
    public boolean isAttached() {
        return attached;
    }

    @Override
    public void detach() {
        invalidate();
    }

    @Override
    public void invalidate() {
        if (!attached) {
            return;
        }
        value = null;
        attached = false;
    }

    @Override
    public T refresh() {
        T oldValue = attached ? value : null;
        boolean hadEffectiveValue = attached;
        T newValue = loader.get();
        value = newValue;
        attached = true;
        if (hadEffectiveValue && !Objects.equals(oldValue, newValue)) {
            fireChanged(oldValue, newValue);
        }
        return newValue;
    }

    private void fireChanged(T oldValue, T newValue) {
        ValueChangeEvent<T> event = new ValueChangeEvent<>(this, oldValue, newValue, ChangeOrigin.MODEL);
        List<ValueChangeListener<T>> snapshot = List.copyOf(listeners);
        for (ValueChangeListener<T> listener : snapshot) {
            listener.valueChanged(event);
        }
    }
}
