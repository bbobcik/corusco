package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.table.ColumnState;
import cz.auderis.corusco.core.table.InMemoryTableStateStore;
import cz.auderis.corusco.core.table.SortDirection;
import cz.auderis.corusco.core.table.SortState;
import cz.auderis.corusco.core.table.TableState;
import cz.auderis.corusco.core.table.TableStateStore;
import java.util.List;

/**
 * Demonstrates descriptor-based table state merge.
 */
public final class TableStateExample {

    private TableStateExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Merges old persisted state with current generated table metadata.
     *
     * @return state diagnostics
     */
    public static List<String> runScenario() {
        TableStateStore store = new InMemoryTableStateStore();
        TableState stored = new TableState(
                GeneratedCustomerRowColumns.TABLE.id(),
                List.of(
                        new ColumnState("legacy/removed", 90, 0, true),
                        new ColumnState("generated-customer-table/orders", 70, 1, false),
                        new ColumnState("generated-customer-table/customer-name", 500, 2, true)
                ),
                List.of(
                        new SortState("legacy/removed", SortDirection.ASCENDING, 0),
                        new SortState("generated-customer-table/orders", SortDirection.DESCENDING, 1)
                )
        );
        store.save(stored);

        // Controllers load by stable generated table id before merging. Missing
        // entries intentionally flow through the same merge path as old data.
        TableState loaded = store.load(GeneratedCustomerRowColumns.TABLE.id()).orElse(null);

        // The descriptor decides which stored columns are still known. Unknown
        // old columns disappear and known widths are clamped to current bounds.
        TableState merged = TableState.merge(GeneratedCustomerRowTableDescriptor.DESCRIPTOR, loaded);
        store.save(merged);
        store.flush();

        ColumnState first = merged.columns().get(0);
        ColumnState second = merged.columns().get(1);

        // Sort metadata is also descriptor-filtered so later JTable controllers
        // do not try to apply stale sorter keys.
        return List.of(
                first.id() + ":" + first.width() + ":" + first.visible(),
                second.id() + ":" + second.width() + ":" + second.visible(),
                merged.sort().getFirst().columnId() + ":" + merged.sort().getFirst().priority()
        );
    }
}
