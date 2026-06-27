package cz.auderis.corusco.core.data;

import java.util.Objects;

/**
 * One generated request for a data page.
 *
 * <p>A request is the immutable value passed from a
 * {@link CoruscoDataSource} to a {@link CoruscoDataLoader}. It captures the
 * page range, query, and refresh generation at the moment work was scheduled.
 * Loaders should treat it as read-only input and return a
 * {@link CoruscoDataPage} for the same logical request.</p>
 *
 * <p>The generation value is owned by the data source. A loader may include it
 * in diagnostics, but it should not interpret the number globally or persist
 * it. Stale-result suppression remains the data source's responsibility.</p>
 *
 * @param range requested row range
 * @param query requested query
 * @param refreshGeneration monotonically increasing refresh generation
 */
public record CoruscoDataRequest(CoruscoDataRange range, CoruscoDataQuery query, long refreshGeneration) {

    /**
     * Creates a request.
     *
     * <p>The range and query must be present, and the generation must be
     * non-negative. Generation zero is reserved for initial state in the
     * default source; scheduled requests normally start at one.</p>
     *
     * @param range range
     * @param query query
     * @param refreshGeneration generation
     */
    public CoruscoDataRequest {
        Objects.requireNonNull(range, "range");
        Objects.requireNonNull(query, "query");
        if (refreshGeneration < 0L) {
            throw new IllegalArgumentException("refreshGeneration must not be negative");
        }
    }
}
