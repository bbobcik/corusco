package cz.auderis.corusco.core.task;

/**
 * Blocking unit of work submitted through a {@link TaskService}.
 *
 * <p>A task service executes this callback away from the caller's immediate
 * control and reports the result through the {@link TaskHandle} returned from
 * submission. Implementations should keep Swing component access out of the
 * task body unless they explicitly marshal that access to the EDT. Long-running
 * tasks are expected to cooperate with cancellation by checking the supplied
 * {@link CancellationToken}.</p>
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
