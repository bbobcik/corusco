package cz.auderis.corusco.core.task;

/**
 * Blocking unit of work submitted through a {@link TaskService}.
 *
 * @param <T> result type
 */
@FunctionalInterface
public interface UiTask<T> {

    /**
     * Runs the task body.
     *
     * <p>Implementations should periodically inspect the supplied cancellation
     * token or call {@link CancellationToken#throwIfCancellationRequested()} in
     * long-running loops.</p>
     *
     * @param cancellation cancellation token
     * @return task result, possibly {@code null}
     * @throws Exception when the task fails
     */
    T run(CancellationToken cancellation) throws Exception;
}
