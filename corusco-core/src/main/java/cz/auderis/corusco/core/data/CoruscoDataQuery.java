package cz.auderis.corusco.core.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Transport-neutral filter and sort query.
 *
 * <p>A query is the stable, UI-independent description of the current result
 * set. Filters narrow the result set; sort terms define ordering. Ranges are
 * intentionally stored in {@link CoruscoDataRequest}, not here, so the same
 * query can be used for several windows or for query-wide selection.</p>
 *
 * <p>Constructor normalization copies both lists and sorts sort terms by their
 * supplied priority. The resulting sort list is then renumbered from zero so
 * equality is stable even when callers provide sparse priorities. Filters keep
 * caller order because some adapters may preserve or report filters in that
 * order.</p>
 *
 * @param filters immutable filter terms
 * @param sort immutable sort terms normalized by priority
 */
public record CoruscoDataQuery(List<CoruscoDataFilter> filters, List<CoruscoDataSort> sort) {

    /**
     * Empty query.
     *
     * <p>The empty query has no filters and no sort terms. It is safe to reuse
     * where an application wants the backend's natural order.</p>
     */
    public static final CoruscoDataQuery EMPTY = new CoruscoDataQuery(List.of(), List.of());

    /**
     * Creates a query.
     *
     * <p>The lists are copied immediately. Later changes to caller-owned lists
     * are not reflected in this query.</p>
     *
     * @param filters filters
     * @param sort sort terms
     */
    public CoruscoDataQuery {
        filters = List.copyOf(Objects.requireNonNull(filters, "filters"));
        sort = normalizeSort(sort);
    }

    private static List<CoruscoDataSort> normalizeSort(List<CoruscoDataSort> sort) {
        Objects.requireNonNull(sort, "sort");
        List<CoruscoDataSort> sorted = new ArrayList<>(sort);
        sorted.sort(Comparator.comparingInt(CoruscoDataSort::priority));
        List<CoruscoDataSort> normalized = new ArrayList<>(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            CoruscoDataSort item = sorted.get(i);
            normalized.add(new CoruscoDataSort(item.fieldId(), item.direction(), i));
        }
        return List.copyOf(normalized);
    }
}
