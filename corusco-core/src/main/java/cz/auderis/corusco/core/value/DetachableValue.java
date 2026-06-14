package cz.auderis.corusco.core.value;

import cz.auderis.corusco.core.lifecycle.Detachable;

/**
 * Readable value that can release and later rebuild its cached value.
 *
 * <p>This interface is for presenter values backed by external or derived data
 * that may be released while the owner remains reusable. The value contract
 * remains synchronous: {@link #value()} returns the current effective value and
 * may load it on demand. Detaching or invalidating releases cached data but
 * does not by itself represent a user-visible value change. {@link #refresh()}
 * is the explicit operation for reloading and notifying subscribers when the
 * effective value changes.</p>
 *
 * <p>Implementors should make attachment state observable through
 * {@link #isAttached()} and should document whether {@link #detach()} and
 * {@link #invalidate()} are equivalent. Subscribers remain owned by the value
 * implementation until their subscriptions are closed.</p>
 *
 * @param <T> value type
 */
public interface DetachableValue<T> extends ReadableValue<T>, Detachable {

    /**
     * Indicates whether this value currently holds an attached cached value.
     *
     * @return {@code true} when a cached value is present
     */
    boolean isAttached();

    /**
     * Invalidates the current cached value without eagerly loading a
     * replacement.
     *
     * <p>This is equivalent to detaching for simple loadable values, but the
     * separate name is useful at call sites that are responding to stale input
     * rather than view lifecycle cleanup.</p>
     */
    void invalidate();

    /**
     * Reloads the value and notifies subscribers when the effective value
     * changes.
     *
     * @return refreshed value, possibly {@code null}
     */
    T refresh();
}
