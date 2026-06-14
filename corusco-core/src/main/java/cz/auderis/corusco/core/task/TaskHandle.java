package cz.auderis.corusco.core.task;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.value.ReadableValue;

import java.util.concurrent.CompletableFuture;

/**
 * Observes and controls one submitted task.
 *
 * <p>The handle exposes task-specific busy state, cooperative cancellation, and
 * a completion future. Closing the handle is equivalent to requesting
 * cancellation; it does not close the owning {@link TaskService}. The
 * completion future represents the final task outcome after the implementation
 * has performed its documented callback and busy-state delivery.</p>
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
     *
     * <p>Cancellation is cooperative. Implementations should signal the token
     * and interrupt or cancel worker execution where their executor supports
     * it, but task bodies are still expected to check the token and stop
     * promptly.</p>
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
