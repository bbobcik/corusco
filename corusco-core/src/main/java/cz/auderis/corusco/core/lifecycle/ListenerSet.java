package cz.auderis.corusco.core.lifecycle;

import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Small copy-on-write listener registry with identity-based set semantics.
 *
 * <p>The set stores each listener object at most once, using object identity
 * rather than {@link Object#equals(Object)}. Registering the same listener
 * instance again returns a no-op subscription and leaves the set unchanged.
 * Removing a listener by instance or closing the subscription returned from
 * {@link #addListener(Object)} affects future events only; any dispatch already
 * using a captured snapshot continues over that snapshot.</p>
 *
 * <p>Listener registration is intentionally treated as the cold path. Add,
 * remove, and clear operations use a private mutation lock and publish a fresh
 * array through a volatile field. Event dispatch remains the hot path: it reads
 * the current volatile array once and invokes listeners without locking.</p>
 *
 * <p>Prefer the returned subscription as the owner of a registration. Explicit
 * {@link #removeListener(Object)} is available for externally managed listener
 * lifecycles, but callers should avoid keeping stale subscriptions for listener
 * instances that they remove and later register again.</p>
 *
 * <p>Events raised re-entrantly, that is by a listener of this set on the
 * thread that is currently dispatching an event of this set, are queued and
 * delivered after the current dispatch reaches every listener. Every listener
 * therefore observes events from one thread in the order they were raised,
 * even when an earlier listener reacts to an event by raising another one.
 * Dispatches started concurrently from different threads are not ordered
 * against each other.</p>
 *
 * @param <LSNR> listener type
 * @param <EVT> event payload type
 */
public final class ListenerSet<LSNR, EVT> {

    private final Object mutationLock = new Object();
    private volatile Object @Nullable [] listeners;
    private volatile @Nullable Thread dispatchingThread;
    private final ArrayDeque<PendingEvent> pendingEvents = new ArrayDeque<>();

    /**
     * Adds a listener when the same listener instance is not already present.
     *
     * @param listener listener instance to register
     * @return subscription that removes this listener instance when closed, or a
     *         no-op subscription when the listener was already registered
     */
    public Subscription addListener(LSNR listener) {
        Objects.requireNonNull(listener, "listener");
        synchronized (mutationLock) {
            final Object @Nullable [] oldListeners = listeners;
            final Object[] newListeners;
            if (null == oldListeners) {
                newListeners = new Object[1];
                newListeners[0] = listener;
            } else {
                for (int index = oldListeners.length - 1; index >= 0; --index) {
                    if (oldListeners[index] == listener) {
                        return NoOperation.INSTANCE;
                    }
                }
                final int arraySize = oldListeners.length;
                newListeners = Arrays.copyOf(oldListeners, arraySize + 1);
                newListeners[arraySize] = listener;
            }
            listeners = newListeners;
        }
        return new ListenerRemover(this, listener);
    }

    /**
     * Removes a listener instance from future dispatches.
     *
     * @param listener listener instance to remove
     */
    public void removeListener(LSNR listener) {
        Objects.requireNonNull(listener, "listener");
        removeListenerInternal(listener);
    }

    private void removeListenerInternal(Object listener) {
        synchronized (mutationLock) {
            final Object @Nullable [] oldListeners = listeners;
            if (null == oldListeners) {
                return;
            }
            final int arraySize = oldListeners.length;
            int index;
            for (index = arraySize - 1; index >= 0; --index) {
                if (listener == oldListeners[index]) break;
            }
            if (index < 0) {
                return;
            }
            final Object @Nullable [] newListeners;
            if (1 == arraySize) {
                newListeners = null;
            } else {
                newListeners = new Object[arraySize - 1];
                if (index > 0) {
                    System.arraycopy(oldListeners, 0, newListeners, 0, index);
                }
                if (index < arraySize - 1) {
                    System.arraycopy(oldListeners, index + 1, newListeners, index, arraySize - 1 - index);
                }
            }
            listeners = newListeners;
        }
    }

    /**
     * Removes all listener registrations from future dispatches.
     */
    public void clearListeners() {
        synchronized (mutationLock) {
            listeners = null;
        }
    }

    /**
     * Delivers an event to the listeners registered at the start of dispatch.
     *
     * <p>The event payload may be {@code null}. Listener additions, removals,
     * and clears performed while dispatch is in progress affect only later
     * dispatches. When this method is called on the thread that is already
     * dispatching an event of this set (for example from a listener that
     * mutates the event source), the new event is queued with the listeners
     * registered at that moment and delivered once the current dispatch has
     * reached all its listeners.</p>
     *
     * @param event event payload, possibly {@code null}
     * @param listenerDispatch dispatcher that invokes one listener with the
     *         event payload
     */
    @SuppressWarnings("unchecked")
    public void fireEvent(@Nullable EVT event, BiConsumer<? super LSNR, ? super @Nullable EVT> listenerDispatch) {
        Objects.requireNonNull(listenerDispatch, "listenerDispatch");
        final Object @Nullable [] currentListeners = listeners;
        if (null == currentListeners) {
            return;
        }
        final Thread currentThread = Thread.currentThread();
        if (dispatchingThread == currentThread) {
            // Only the dispatching thread touches pendingEvents.
            pendingEvents.addLast(new PendingEvent(currentListeners, event, listenerDispatch));
            return;
        }
        if (!DISPATCHING_THREAD.compareAndSet(this, (Thread) null, currentThread)) {
            // Another thread is dispatching; concurrent dispatches are unordered.
            dispatch(currentListeners, event, listenerDispatch);
            return;
        }
        try {
            dispatch(currentListeners, event, listenerDispatch);
            PendingEvent pending;
            while (null != (pending = pendingEvents.pollFirst())) {
                dispatch(pending.listeners(), pending.event(), pending.dispatch());
            }
        } finally {
            pendingEvents.clear();
            dispatchingThread = null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <LSNR, EVT> void dispatch(
            Object[] currentListeners,
            @Nullable EVT event,
            BiConsumer<? super LSNR, ? super @Nullable EVT> listenerDispatch
    ) {
        for (Object lsnrObj : currentListeners) {
            final LSNR lsnr = (LSNR) lsnrObj;
            listenerDispatch.accept(lsnr, event);
        }
    }

    @SuppressWarnings("rawtypes")
    private record PendingEvent(Object[] listeners, @Nullable Object event, BiConsumer dispatch) {
    }

    private static final VarHandle DISPATCHING_THREAD;
    static {
        try {
            DISPATCHING_THREAD = MethodHandles
                    .lookup()
                    .findVarHandle(ListenerSet.class, "dispatchingThread", Thread.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final class ListenerRemover implements Subscription {
        private static final VarHandle LISTENER;
        static {
            try {
                LISTENER = MethodHandles
                        .lookup()
                        .findVarHandle(ListenerRemover.class, "listener", Object.class);
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        private @Nullable ListenerSet<?,?> parent;

        @SuppressWarnings("FieldMayBeFinal")
        private volatile @Nullable Object listener;

        ListenerRemover(ListenerSet<?,?> parent, Object listener) {
            this.parent = parent;
            this.listener = listener;
        }

        @Override
        public void close() {
            final Object listenerToRemove = LISTENER.getAndSet(this, null);
            if (null != listenerToRemove) {
                assert null != parent;
                parent.removeListenerInternal(listenerToRemove);
                parent = null;
            }
        }
    }

}
