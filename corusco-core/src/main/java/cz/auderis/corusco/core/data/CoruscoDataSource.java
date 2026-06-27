package cz.auderis.corusco.core.data;

import cz.auderis.corusco.core.collection.ObservableReadableCollection;
import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.ReadableValue;
import org.jspecify.annotations.NonNull;

/**
 * Observable, key-aware current data window.
 *
 * <p>A data source owns the presenter-facing state for one currently visible
 * data page. It exposes row identity, rows, status, range, query, and total
 * count as small observable contracts that a UI adapter or presenter can bind
 * to. The source does not define how rows are rendered, persisted, filtered by
 * a database, or transferred over a network.</p>
 *
 * <p>Rows are exposed through an
 * {@link cz.auderis.corusco.core.collection.ObservableReadableCollection}
 * because consumers should not mutate a loaded page directly. Request methods
 * are the mutation boundary. Implementations may keep old rows visible while a
 * new request is loading and should document their precise refresh and failure
 * behavior.</p>
 *
 * <p>The {@link ChangeOrigin} supplied to request methods is propagated to
 * model updates caused by the request. A direct user search can therefore be
 * distinguished from a presenter refresh or a framework-driven reload by the
 * same origin mechanism used by other core observable models.</p>
 *
 * @param <R> row type
 * @param <K> key type
 */
public interface CoruscoDataSource<R extends @NonNull Object, K extends @NonNull Object> extends Disposable {

    /**
     * Returns row identity metadata.
     *
     * <p>The identity describes how row keys are extracted for selection,
     * edit staging, master-detail routing, and refresh continuity.</p>
     *
     * @return row identity
     */
    CoruscoRowIdentity<R, K> rowIdentity();

    /**
     * Returns current visible rows.
     *
     * <p>The collection represents the currently accepted page. A refresh may
     * leave this collection unchanged until a newer page succeeds.</p>
     *
     * @return observable rows
     */
    ObservableReadableCollection<R> rows();

    /**
     * Returns current status.
     *
     * <p>Status carries loading, ready, failed, and closed state plus the
     * generation that produced that state. Failure details live here rather
     * than in the row collection.</p>
     *
     * @return status value
     */
    ReadableValue<CoruscoDataStatus> status();

    /**
     * Returns current range.
     *
     * @return range value
     */
    ReadableValue<CoruscoDataRange> range();

    /**
     * Returns current query.
     *
     * @return query value
     */
    ReadableValue<CoruscoDataQuery> query();

    /**
     * Returns current total count.
     *
     * <p>The count may be unknown, estimated, or exact. A remote data source
     * can therefore show rows before an expensive exact count is available.</p>
     *
     * @return count value
     */
    ReadableValue<CoruscoDataCount> totalCount();

    /**
     * Requests a page using the current query.
     *
     * <p>This is a range-only navigation operation: it preserves the current
     * query value and requests a different window over the same result set.</p>
     *
     * @param range requested range
     * @param origin change origin for model updates
     */
    void request(CoruscoDataRange range, ChangeOrigin origin);

    /**
     * Changes query and requests a page.
     *
     * <p>This is the main search/filter/sort operation. Implementations should
     * treat the supplied query and range as the next current request and ensure
     * older asynchronous completions cannot overwrite the newer state.</p>
     *
     * @param range requested range
     * @param query query
     * @param origin change origin for model updates
     */
    void request(CoruscoDataRange range, CoruscoDataQuery query, ChangeOrigin origin);

    /**
     * Reloads the current range and query.
     *
     * <p>Refresh is useful after an external save, a manual reload command, or
     * a timed invalidation. It should not implicitly change selection or edit
     * staging; those are separate presenter concerns.</p>
     *
     * @param origin change origin for model updates
     */
    void refresh(ChangeOrigin origin);
}
