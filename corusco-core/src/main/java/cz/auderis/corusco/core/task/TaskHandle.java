package cz.auderis.corusco.core.task;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.value.ReadableValue;

import java.util.concurrent.CompletableFuture;

/**
 * Handle returned for a submitted task.
 *
 * @param <T> result type
 */
public interface TaskHandle<T> extends Disposable {

    /**
     * Returns task-level busy state.
     *
     * @return busy value
     */
    ReadableValue<Boolean> busy();

    /**
     * Returns task cancellation token.
     *
     * @return cancellation token
     */
    CancellationToken cancellationToken();

    /**
     * Completion future that finishes after callback-executor delivery has
     * updated busy state and invoked callbacks.
     *
     * @return completion future
     */
    CompletableFuture<T> completion();

    /**
     * Requests cancellation.
     */
    void cancel();

    /**
     * Indicates whether this task has finished.
     *
     * @return {@code true} after success, failure, or cancellation
     */
    boolean isDone();

    /**
     * Cancels the task.
     */
    @Override
    default void close() {
        cancel();
    }
}
