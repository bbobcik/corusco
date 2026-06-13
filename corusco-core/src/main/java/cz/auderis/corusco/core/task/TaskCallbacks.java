package cz.auderis.corusco.core.task;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Callback hooks for an asynchronous task.
 *
 * <p>Callbacks are invoked by the {@link TaskService}'s configured callback
 * executor after the task leaves the busy state. Cancellation invokes
 * {@link #cancelled()} and suppresses success/failure callbacks.</p>
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
