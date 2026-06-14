package cz.auderis.corusco.core.task;

/**
 * Signals that a cooperative task stopped because cancellation was requested.
 *
 * <p>The task APIs do not interrupt arbitrary code or force a cancellation
 * strategy. Implementations throw this exception when they choose the
 * exception-based path for a cancelled operation, usually after observing a
 * {@link CancellationToken}. Callers should treat it as an expected task
 * outcome rather than an infrastructure failure.</p>
 */
public final class TaskCancelledException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a cancellation exception.
     *
     * @param message detail message
     */
    public TaskCancelledException(String message) {
        super(message);
    }
}
