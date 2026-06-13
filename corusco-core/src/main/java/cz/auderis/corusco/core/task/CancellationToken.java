package cz.auderis.corusco.core.task;

/**
 * Cooperative cancellation signal passed to asynchronous task bodies.
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
