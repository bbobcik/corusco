package cz.auderis.corusco.core.data;

import java.util.Objects;

/**
 * One stable sort term.
 *
 * <p>A sort term names a stable field or column id, a direction, and a
 * priority. It is suitable for service-side sorting, table-state translation,
 * and query persistence because it does not depend on localized column
 * headers or current view indexes.</p>
 *
 * <p>Sort terms are usually normalized by {@link CoruscoDataQuery}. Callers
 * may create terms with sparse priorities while assembling a query; the query
 * constructor sorts by priority and renumbers terms from zero.</p>
 *
 * @param fieldId stable field or column id
 * @param direction sort direction
 * @param priority zero-based sort priority
 */
public record CoruscoDataSort(String fieldId, Direction direction, int priority) {

    /**
     * Sort direction.
     *
     * <p>The directions intentionally mirror table sort directions without
     * depending on Swing or any backend-specific ordering syntax.</p>
     */
    public enum Direction {
        ASCENDING,
        DESCENDING
    }

    /**
     * Creates a sort term.
     *
     * <p>The field id must be non-blank and the priority must be non-negative.
     * The constructor does not check whether the field is sortable for a
     * particular data source; adapters should validate supported ids and
     * capabilities when translating a query.</p>
     *
     * @param fieldId stable field or column id
     * @param direction direction
     * @param priority zero-based priority
     */
    public CoruscoDataSort {
        fieldId = CoruscoDataFilter.requireId(fieldId, "fieldId");
        Objects.requireNonNull(direction, "direction");
        if (priority < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
    }
}
