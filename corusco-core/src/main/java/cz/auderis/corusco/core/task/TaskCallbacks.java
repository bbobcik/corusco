package cz.auderis.corusco.core.task;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Callback hooks for an asynchronous task.
 *
 * <p>{@link TaskService} invokes these hooks to report the terminal outcome of
 * a submitted {@link UiTask}. The callbacks are grouped so callers can pass one
 * lifecycle object to the service instead of three unrelated lambdas. Callback
 * execution thread is chosen by the task service implementation or factory; a
 * Swing-aware service uses the Event Dispatch Thread for terminal callbacks.</p>
 *
 * <p>Only one terminal path should run for a task. Successful completion calls
 * {@link #succeeded(Object)}, unexpected failure calls {@link #failed(Throwable)},
 * and cooperative cancellation calls {@link #cancelled()} while suppressing
 * success/failure callbacks. Implementations should return quickly because they
 * may run on a UI callback executor.</p>
 *
 * @param <T> result type
 */
public interface TaskCallbacks<T> {

    /**
     * No-op callbacks.
     */
    TaskCallbacks<Object> NONE = new TaskCallbacks<>() {
    };

    /**
     * Called when the task succeeds.
     *
     * @param result task result, possibly {@code null}
     */
    default void succeeded(T result) {
    }

    /**
     * Called when the task fails.
     *
     * @param error task failure
     */
    default void failed(Throwable error) {
    }

    /**
     * Called when the task is cancelled.
     */
    default void cancelled() {
    }

    /**
     * Returns no-op callbacks.
     *
     * @param <T> result type
     * @return no-op callbacks
     */
    @SuppressWarnings("unchecked")
    static <T> TaskCallbacks<T> none() {
        return (TaskCallbacks<T>) NONE;
    }

    /**
     * Creates success-only callbacks.
     *
     * @param success success consumer
     * @param <T> result type
     * @return callbacks
     */
    static <T> TaskCallbacks<T> onSuccess(Consumer<? super T> success) {
        Objects.requireNonNull(success, "success");
        return new TaskCallbacks<>() {
            @Override
            public void succeeded(T result) {
                success.accept(result);
            }
        };
    }
}
