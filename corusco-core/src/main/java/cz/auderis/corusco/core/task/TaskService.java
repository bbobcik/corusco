package cz.auderis.corusco.core.task;

import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.value.ReadableValue;

import java.util.concurrent.Executor;

/**
 * Runs blocking UI-initiated work away from the caller thread.
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
