package cz.auderis.corusco.core.data;

import cz.auderis.corusco.core.table.SortDirection;
import cz.auderis.corusco.core.table.SortState;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Converts table-state metadata into data queries.
 *
 * <p>This utility is the narrow bridge between descriptor-backed table state
 * and transport-neutral data queries. It maps {@link TableState#sort()} terms
 * to {@link CoruscoDataSort} terms by stable column persistence id. It does
 * not inspect Swing columns, localized headers, renderers, or row values.</p>
 *
 * <p>Only sort terms whose column ids are still present in the current
 * descriptor are retained. This mirrors table-state merge behavior: stale
 * persisted ids should not leak into a server request after columns are
 * removed or renamed. Filter construction remains a caller responsibility
 * because table state currently stores sort and layout, not arbitrary filter
 * expressions.</p>
 */
public final class CoruscoTableDataQueries {

    private CoruscoTableDataQueries() {
    }

    /**
     * Creates a data query from table sort state.
     *
     * <p>The supplied filter list is copied by the created
     * {@link CoruscoDataQuery}. Sort terms are collected from the table state
     * in priority order and then normalized by the query constructor. Unknown
     * column ids are ignored rather than reported as errors so saved state can
     * survive descriptor evolution.</p>
     *
     * @param descriptor table descriptor whose persistence ids are accepted
     * @param state table state
     * @param filters base filters
     * @param <R> row type
     * @return data query
     */
    public static <R> CoruscoDataQuery fromTableState(
            TableDescriptor<R> descriptor,
            TableState state,
            List<CoruscoDataFilter> filters
    ) {
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(filters, "filters");
        Set<String> knownIds = HashSet.newHashSet(descriptor.columns().size());
        for (var column : descriptor.columns()) {
            knownIds.add(column.descriptor().persistence().id());
        }
        List<CoruscoDataSort> sort = new ArrayList<>(state.sort().size());
        for (SortState item : state.sort()) {
            if (!knownIds.contains(item.columnId())) {
                continue;
            }
            sort.add(new CoruscoDataSort(item.columnId(), direction(item.direction()), item.priority()));
        }
        return new CoruscoDataQuery(filters, sort);
    }

    /**
     * Creates a sort-only data query from table state.
     *
     * <p>This overload is useful when the caller has no data-layer filters and
     * only wants to translate the persisted table sort order.</p>
     *
     * @param descriptor table descriptor
     * @param state table state
     * @param <R> row type
     * @return data query
     */
    public static <R> CoruscoDataQuery fromTableState(TableDescriptor<R> descriptor, TableState state) {
        return fromTableState(descriptor, state, List.of());
    }

    private static CoruscoDataSort.Direction direction(SortDirection direction) {
        return direction == SortDirection.ASCENDING
                ? CoruscoDataSort.Direction.ASCENDING
                : CoruscoDataSort.Direction.DESCENDING;
    }
}
