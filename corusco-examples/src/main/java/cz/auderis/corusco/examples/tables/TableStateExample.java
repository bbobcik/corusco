package cz.auderis.corusco.examples.tables;

import cz.auderis.corusco.examples.generated.*;

import cz.auderis.corusco.core.table.ColumnState;
import cz.auderis.corusco.core.table.InMemoryTableStateStore;
import cz.auderis.corusco.core.table.SortDirection;
import cz.auderis.corusco.core.table.SortState;
import cz.auderis.corusco.core.table.TableState;
import cz.auderis.corusco.core.table.TableStateMigration;
import cz.auderis.corusco.core.table.TableStateStore;
import java.util.List;

/**
 * Demonstrates descriptor-based table state merge.
 *
 * <p>The scenario merges saved table state with a descriptor so unknown columns
 * are ignored and new descriptor columns receive defaults. It is aimed at
 * readers adding user-customizable table layouts.</p>
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
                0,
                GeneratedCustomerRowColumns.TABLE.id(),
                List.of(
                        new ColumnState("legacy/removed", 90, 0, true),
                        new ColumnState("generated-customer-table/orders", 70, 1, false),
                        new ColumnState("generated-customer-table/old-name", 500, 2, true)
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
        TableStateMigration<GeneratedCustomerRow> migration = (descriptor, state) -> new TableState(
                TableState.DEFAULT_SCHEMA_VERSION,
                state.tableId(),
                state.columns().stream()
                        .map(column -> column.id().equals("generated-customer-table/old-name")
                                ? new ColumnState(
                                        "generated-customer-table/customer-name",
                                        column.width(),
                                        column.order(),
                                        column.visible()
                                )
                                : column)
                        .toList(),
                state.sort()
        );

        // The migration hook handles known schema changes first. The
        // descriptor merge then drops truly unknown old columns and clamps
        // migrated widths to current generated bounds.
        TableState merged = TableState.merge(GeneratedCustomerRowTableDescriptor.DESCRIPTOR, loaded, migration);
        store.save(merged);
        store.flush();

        ColumnState first = merged.columns().get(0);
        ColumnState second = merged.columns().get(1);

        // Sort metadata is also descriptor-filtered so later JTable controllers
        // do not try to apply stale sorter keys.
        return List.of(
                first.id() + ":" + first.width() + ":" + first.visible(),
                second.id() + ":" + second.width() + ":" + second.visible(),
                Integer.toString(merged.schemaVersion()),
                merged.sort().getFirst().columnId() + ":" + merged.sort().getFirst().priority()
        );
    }
}
