package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.Subscription;

/**
 * A synchronously observable value.
 *
 * <p>This is the scalar observation contract used by field models, commands,
 * task handles, busy indicators, and derived presenter state. Callers read the
 * current value with {@link #value()} and subscribe to effective changes when a
 * view or derived model must stay current.</p>
 *
 * <p>Subscribing returns a {@link Subscription} that owns the listener
 * registration. Closing the subscription stops future events and is idempotent.
 * Implementations in this package deliver events synchronously on the thread
 * performing the mutation and make no cross-thread safety guarantees unless
 * stated otherwise. Values may be {@code null}.</p>
 *
 * <p>Implementors must document any stronger threading, caching, or lifecycle
 * behavior. They should avoid emitting events for unchanged effective values
 * unless a concrete implementation explicitly documents that pattern.</p>
 *
 * @param <T> value type
 */
public interface ReadableValue<T> {

    /**
     * Returns the current value.
     *
     * @return current value, possibly {@code null}
     */
    T value();

    /**
     * Subscribes a listener to effective value changes.
     *
     * <p>Setting an equal value does not emit an event for the built-in
     * implementations. Listener removal during dispatch affects future events
     * but does not corrupt the current snapshot dispatch.</p>
     *
     * @param listener listener to register
     * @return subscription that removes the listener when closed
     */
    Subscription subscribe(ValueChangeListener<T> listener);
}
