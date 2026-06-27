package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;
import org.jspecify.annotations.Nullable;

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
 * <p>Lazy loading through {@link #value()} never emits an event because it only
 * materializes the current effective value for the caller. Explicit refreshes
 * that change an already attached value emit
 * {@link StandardChangeOrigin#MODEL}, reflecting a model or presenter-owned
 * reload rather than a direct user edit or binding echo.</p>
 *
 * <p>The loader is invoked on the caller's thread and may return {@code null}.
 * Cache attachment is tracked separately from the cached object, so a loaded
 * {@code null} is still an attached value. This class is intentionally not
 * synchronized; it follows the value package convention of single-owner
 * presentation state.</p>
 *
 * <p>The instance retains subscribers until their subscriptions are closed. It
 * does not own external resources used by the loader.</p>
 *
 * @param <T> value type; loaded values may be {@code null}
 */
public final class LoadableValue<T> implements DetachableValue<T> {

    private final Supplier<? extends @Nullable T> loader;
    private final ListenerSet<ValueChangeListener<T>, ValueChangeEvent<T>> listeners = new ListenerSet<>();
    private boolean attached;
    private @Nullable T value;

    /**
     * Creates a lazy value backed by a loader.
     *
     * @param loader loader invoked when the value is first accessed after
     *         construction or detach; may return {@code null}
     */
    public LoadableValue(Supplier<? extends @Nullable T> loader) {
        this.loader = Objects.requireNonNull(loader, "loader");
    }

    /**
     * Creates a lazy value backed by a loader.
     *
     * @param loader loader invoked when the value is first accessed after
     *         construction or detach; may return {@code null}
     * @param <T> value type
     * @return loadable value
     */
    public static <T> LoadableValue<T> of(Supplier<? extends @Nullable T> loader) {
        return new LoadableValue<>(loader);
    }

    @Override
    public @Nullable T value() {
        if (!attached) {
            value = loader.get();
            attached = true;
        }
        return value;
    }

    @Override
    public Subscription subscribe(ValueChangeListener<T> listener) {
        return listeners.addListener(listener);
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
    public @Nullable T refresh() {
        @Nullable T oldValue = attached ? value : null;
        boolean hadEffectiveValue = attached;
        @Nullable T newValue = loader.get();
        value = newValue;
        attached = true;
        if (hadEffectiveValue && !Objects.equals(oldValue, newValue)) {
            fireChanged(oldValue, newValue);
        }
        return newValue;
    }

    private void fireChanged(@Nullable T oldValue, @Nullable T newValue) {
        ValueChangeEvent<T> event = new ValueChangeEvent<>(this, oldValue, newValue, StandardChangeOrigin.MODEL);
        listeners.fireEvent(event, ValueChangeListener::valueChanged);
    }
}
