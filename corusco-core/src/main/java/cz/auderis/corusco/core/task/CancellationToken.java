package cz.auderis.corusco.core.task;

/**
 * Cooperative cancellation signal passed to asynchronous task bodies.
 *
 * <p>The token is read-only from the task's perspective. Task implementations
 * should check it before expensive work, after blocking operations, and before
 * publishing results. Throwing {@link TaskCancelledException} is the standard
 * way for a task body to report that it stopped because cancellation was
 * requested.</p>
 */
public interface CancellationToken {

    /**
     * Indicates whether cancellation has been requested.
     *
     * @return {@code true} after cancellation has been requested
     */
    boolean isCancellationRequested();

    /**
     * Throws when cancellation has been requested.
     *
     * @throws TaskCancelledException when cancellation has been requested
     */
    default void throwIfCancellationRequested() {
        if (isCancellationRequested()) {
            throw new TaskCancelledException("Task was cancelled");
        }
    }
}
