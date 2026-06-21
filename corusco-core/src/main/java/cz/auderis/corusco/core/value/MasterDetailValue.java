package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.ListenerSet;
import cz.auderis.corusco.core.lifecycle.Subscription;

import java.util.Objects;
import java.util.function.Function;

/**
 * Lazy detachable detail value driven by a master value.
 *
 * <p>Use this class for master-detail presentation state, such as loading a
 * detail record or derived editor model from a selected row. The helper owns a
 * subscription to the master value and uses the supplied loader to compute the
 * detail. The detail loader receives the current master value, which may be
 * {@code null}. Detail data loads lazily on {@link #value()} or
 * {@link #refresh()}. When the detail is already attached, master changes
 * reload immediately and notify subscribers if the effective detail changes.
 * When detached, master changes only invalidate the stale association; no
 * detail load occurs until the next access.</p>
 *
 * <p>Call {@link #close()} when the presenter lifecycle ends to remove the
 * master subscription, invalidate cached detail state, and clear detail
 * listeners. The class is synchronous, not synchronized, and inherits the
 * threading assumptions of the master value and loader.</p>
 *
 * @param <M> master value type
 * @param <T> detail value type
 */
public final class MasterDetailValue<M, T> implements DetachableValue<T>, Disposable {

    private final ReadableValue<M> master;
    private final Function<? super M, ? extends T> loader;
    private final Subscription masterSubscription;
    private final ListenerSet<ValueChangeListener<T>, ValueChangeEvent<T>> listeners = new ListenerSet<>();
    private boolean attached;
    private boolean closed;
    private M attachedMaster;
    private T value;

    /**
     * Creates a master-detail value.
     *
     * @param master master value
     * @param loader detail loader invoked with the current master value
     * @param <M> master value type
     * @param <T> detail value type
     * @return master-detail value
     */
    public static <M, T> MasterDetailValue<M, T> of(
            ReadableValue<M> master,
            Function<? super M, ? extends T> loader
    ) {
        return new MasterDetailValue<>(master, loader);
    }

    /**
     * Creates a master-detail value.
     *
     * @param master master value
     * @param loader detail loader invoked with the current master value
     */
    public MasterDetailValue(ReadableValue<M> master, Function<? super M, ? extends T> loader) {
        this.master = Objects.requireNonNull(master, "master");
        this.loader = Objects.requireNonNull(loader, "loader");
        this.masterSubscription = master.subscribe(this::masterChanged);
    }

    @Override
    public T value() {
        if (closed) {
            return value;
        }
        M currentMaster = master.value();
        if (!attached || !Objects.equals(attachedMaster, currentMaster)) {
            load(currentMaster);
        }
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
    public boolean isAttached() {
        return attached;
    }

    @Override
    public void detach() {
        invalidate();
    }

    @Override
    public void invalidate() {
        attached = false;
        attachedMaster = null;
        value = null;
    }

    @Override
    public T refresh() {
        if (closed) {
            return value;
        }
        M currentMaster = master.value();
        T oldValue = attached ? value : null;
        boolean hadEffectiveValue = attached;
        T newValue = load(currentMaster);
        if (hadEffectiveValue && !Objects.equals(oldValue, newValue)) {
            fireChanged(oldValue, newValue, StandardChangeOrigin.MODEL);
        }
        return newValue;
    }

    /**
     * Closes the master subscription and removes detail listeners.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            masterSubscription.close();
        } finally {
            invalidate();
            listeners.clearListeners();
        }
    }

    private void masterChanged(ValueChangeEvent<M> event) {
        if (closed) {
            return;
        }
        if (!attached) {
            attachedMaster = null;
            return;
        }
        T oldValue = value;
        T newValue = load(event.newValue());
        if (!Objects.equals(oldValue, newValue)) {
            fireChanged(oldValue, newValue, event.origin());
        }
    }

    private T load(M currentMaster) {
        T loaded = loader.apply(currentMaster);
        attachedMaster = currentMaster;
        value = loaded;
        attached = true;
        return loaded;
    }

    private void fireChanged(T oldValue, T newValue, ChangeOrigin origin) {
        ValueChangeEvent<T> event = new ValueChangeEvent<>(this, oldValue, newValue, origin);
        listeners.fireEvent(event, ValueChangeListener::valueChanged);
    }
}
