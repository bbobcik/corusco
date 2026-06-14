package cz.auderis.corusco.core.task;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.value.ReadableValue;

import java.util.concurrent.Executor;

/**
 * Runs UI-initiated work away from the caller thread and reports completion
 * through callbacks.
 *
 * <p>A task service is a lifecycle-owned presenter or view helper. It accepts
 * {@link UiTask} bodies, passes each task a cooperative
 * {@link CancellationToken}, exposes service-level busy state, and returns a
 * {@link TaskHandle} for task-level observation and cancellation. The interface
 * does not mandate a threading model for callbacks; implementations document
 * which executor or thread is used.</p>
 *
 * <p>Implementation contract: implementations must reject new submissions after
 * {@link #close()} and should make close idempotent. They should not invoke
 * success callbacks after a task has been cancelled.</p>
 */
public interface TaskService extends Disposable {

    /**
     * Creates a virtual-thread-backed task service.
     *
     * @param callbackExecutor executor used for callbacks and busy-state
     *         completion delivery
     * @return task service
     */
    static TaskService virtualThreads(Executor callbackExecutor) {
        return DefaultTaskService.virtualThreads(callbackExecutor);
    }

    /**
     * Returns service-level busy state.
     *
     * @return busy value
     */
    ReadableValue<Boolean> busy();

    /**
     * Submits a task with no-op callbacks.
     *
     * @param task task body
     * @param <T> result type
     * @return task handle
     */
    default <T> TaskHandle<T> submit(UiTask<? extends T> task) {
        return submit(task, TaskCallbacks.none());
    }

    /**
     * Submits a task.
     *
     * @param task task body
     * @param callbacks callbacks delivered through the configured callback
     *         executor
     * @param <T> result type
     * @return task handle
     */
    <T> TaskHandle<T> submit(UiTask<? extends T> task, TaskCallbacks<? super T> callbacks);
}
