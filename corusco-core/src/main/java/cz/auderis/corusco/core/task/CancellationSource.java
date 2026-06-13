package cz.auderis.corusco.core.task;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mutable owner of a {@link CancellationToken}.
 */
public final class CancellationSource {

    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final CancellationToken token = cancelled::get;

    /**
     * Returns the read-only cancellation token.
     *
     * @return cancellation token
     */
    public CancellationToken token() {
        return token;
    }

    /**
     * Requests cancellation.
     *
     * @return {@code true} when this call changed the state
     */
    public boolean cancel() {
        return cancelled.compareAndSet(false, true);
    }

    /**
     * Indicates whether cancellation has been requested.
     *
     * @return {@code true} after cancellation has been requested
     */
    public boolean isCancellationRequested() {
        return cancelled.get();
    }
}
