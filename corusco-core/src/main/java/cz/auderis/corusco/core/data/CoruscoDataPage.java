package cz.auderis.corusco.core.data;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Loaded data page.
 *
 * <p>A page is the immutable result produced by a
 * {@link CoruscoDataLoader}. It contains the rows accepted for the requested
 * window, the range that those rows represent, and the backend's current count
 * knowledge. The record is independent of the data source that requested it;
 * tests and adapters can create pages directly.</p>
 *
 * <p>The row list is copied with {@link List#copyOf(java.util.Collection)}.
 * Row objects themselves are not cloned. Applications should therefore use row
 * objects with the mutability policy their presenter expects, typically
 * immutable records for table read models.</p>
 *
 * @param rows immutable row snapshot
 * @param range loaded range
 * @param totalCount total count state
 * @param <R> row type
 */
public record CoruscoDataPage<R extends @NonNull Object>(
        List<R> rows,
        CoruscoDataRange range,
        CoruscoDataCount totalCount
) {

    /**
     * Creates a page.
     *
     * <p>The number of rows must not exceed the range limit. Short pages are
     * allowed because a backend may reach the end of a result set or return a
     * smaller window for policy reasons. The constructor does not require the
     * range offset to match any row key or domain value.</p>
     *
     * @param rows rows
     * @param range loaded range
     * @param totalCount total count state
     */
    public CoruscoDataPage {
        rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
        Objects.requireNonNull(range, "range");
        Objects.requireNonNull(totalCount, "totalCount");
        if (rows.size() > range.limit()) {
            throw new IllegalArgumentException("rows size must not exceed range limit");
        }
    }
}
