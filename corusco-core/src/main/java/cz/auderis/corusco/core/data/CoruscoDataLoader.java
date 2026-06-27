package cz.auderis.corusco.core.data;

import cz.auderis.corusco.core.task.CancellationToken;
import org.jspecify.annotations.NonNull;

/**
 * Synchronous, technology-neutral data page loader.
 *
 * <p>A loader is the boundary where an application adapts Corusco's query
 * model to a concrete backing technology. Implementations may call a database,
 * REST endpoint, in-memory repository, file index, or service facade. The
 * interface stays synchronous so a {@link CoruscoDataSource} can decide how to
 * schedule it through a {@link cz.auderis.corusco.core.task.TaskService}.</p>
 *
 * <p>Loaders should check the supplied {@link CancellationToken} before and
 * after expensive work and before returning a page. Throwing an exception is
 * the normal way to report backend failure; the default data source records
 * that failure in {@link CoruscoDataStatus}.</p>
 *
 * @param <R> row type
 */
@FunctionalInterface
public interface CoruscoDataLoader<R extends @NonNull Object> {

    /**
     * Loads one page.
     *
     * <p>The returned page should correspond to the supplied request's range
     * and query. The interface does not require the same range instance in the
     * returned page, because a backend may clamp or otherwise report the loaded
     * range explicitly.</p>
     *
     * @param request data request
     * @param cancellation cancellation token
     * @return loaded page
     * @throws Exception when the backing technology fails
     */
    CoruscoDataPage<R> load(CoruscoDataRequest request, CancellationToken cancellation) throws Exception;
}
