package cz.auderis.corusco.core.data;

/**
 * Requested or loaded data window.
 *
 * <p>A range uses zero-based logical offsets, not Swing view rows or row
 * keys. It is suitable for page-style requests such as "load at most 100 rows
 * starting at offset 200." The range says nothing about the total count or
 * whether the returned page is the last page; that belongs to
 * {@link CoruscoDataCount}.</p>
 *
 * <p>Offsets use {@code long} so a backend can represent large result sets.
 * Limits use {@code int} because a single loaded window should remain bounded
 * by an in-memory collection size.</p>
 *
 * @param offset zero-based row offset
 * @param limit maximum number of rows in the window
 */
public record CoruscoDataRange(long offset, int limit) {

    /**
     * Empty range at offset zero.
     *
     * <p>This constant is used by newly created data sources before their
     * first request.</p>
     */
    public static final CoruscoDataRange EMPTY = new CoruscoDataRange(0, 0);

    /**
     * Creates a range.
     *
     * <p>Both values must be non-negative. A zero limit is valid and
     * represents an empty window.</p>
     *
     * @param offset zero-based row offset
     * @param limit maximum row count
     */
    public CoruscoDataRange {
        if (offset < 0L) {
            throw new IllegalArgumentException("offset must not be negative");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative");
        }
    }
}
