package cz.auderis.corusco.core.task;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mutable owner of a cooperative {@link CancellationToken}.
 *
 * <p>A cancellation source is created by code that owns a task or workflow and
 * handed to collaborators through its read-only {@link #token()}. Calling
 * {@link #cancel()} permanently flips the token into the cancelled state; task
 * code decides where to check the token and whether to throw
 * {@link TaskCancelledException} or return normally. The state transition is
 * atomic and idempotent, so multiple cancellation requests are safe and only
 * the first reports that it changed state.</p>
 */
public final class CancellationSource {

    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final CancellationToken token = cancelled::get;

    /**
     * Creates a cancellation source whose token is not yet cancelled.
     */
    public CancellationSource() {
    }

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
