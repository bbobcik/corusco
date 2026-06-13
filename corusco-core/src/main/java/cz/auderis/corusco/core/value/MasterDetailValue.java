package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Lazy detachable detail value driven by a master value.
 *
 * <p>The detail loader receives the current master value, which may be
 * {@code null}. Detail data loads lazily on {@link #value()} or
 * {@link #refresh()}. When the detail is already attached, master changes
 * reload immediately and notify subscribers if the effective detail changes.
 * When detached, master changes only invalidate the stale association; no
 * detail load occurs until the next access.</p>
 *
 * <p>The helper owns a subscription to the master value. Call {@link #close()}
 * when the presenter lifecycle ends to remove that subscription and clear
 * detail listeners.</p>
 *
 * @param <M> master value type
 * @param <T> detail value type
 */
public final class MasterDetailValue<M, T> implements DetachableValue<T>, Disposable {

    private final ReadableValue<M> master;
    private final Function<? super M, ? extends T> loader;
    private final Subscription masterSubscription;
    private final List<ValueChangeListener<T>> listeners = new ArrayList<>();
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
            fireChanged(oldValue, newValue, ChangeOrigin.MODEL);
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
            listeners.clear();
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
        List<ValueChangeListener<T>> snapshot = List.copyOf(listeners);
        for (ValueChangeListener<T> listener : snapshot) {
            listener.valueChanged(event);
        }
    }
}
