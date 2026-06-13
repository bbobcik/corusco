package cz.auderis.corusco.core.table;

import cz.auderis.corusco.core.key.ResourceKey;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TableStateTest {

    @Test
    void defaultStateFollowsDescriptorDefaults() {
        TableState state = TableState.defaults(customerTable());

        assertThat(state.schemaVersion()).isEqualTo(TableState.DEFAULT_SCHEMA_VERSION);
        assertThat(state.tableId()).isEqualTo("customers");
        assertThat(state.columns()).containsExactly(
                new ColumnState("customers/name", 160, 0, true),
                new ColumnState("customers/orders", 80, 1, true),
                new ColumnState("customers/status", 120, 2, false)
        );
        assertThat(state.sort()).isEmpty();
    }

    @Test
    void mergeAppliesMigrationBeforeDescriptorFiltering() {
        TableState stored = new TableState(
                7,
                "customers",
                List.of(new ColumnState("customers/old-name", 200, 0, true)),
                List.of(new SortState("customers/old-name", SortDirection.ASCENDING, 0))
        );
        TableStateMigration<CustomerRow> migration = (descriptor, state) -> new TableState(
                TableState.DEFAULT_SCHEMA_VERSION,
                state.tableId(),
                state.columns().stream()
                        .map(column -> column.id().equals("customers/old-name")
                                ? new ColumnState("customers/name", column.width(), column.order(), column.visible())
                                : column)
                        .toList(),
                state.sort().stream()
                        .map(sort -> sort.columnId().equals("customers/old-name")
                                ? new SortState("customers/name", sort.direction(), sort.priority())
                                : sort)
                        .toList()
        );

        TableState merged = TableState.merge(customerTable(), stored, migration);

        assertThat(merged.schemaVersion()).isEqualTo(TableState.DEFAULT_SCHEMA_VERSION);
        assertThat(merged.columns().getFirst()).isEqualTo(new ColumnState("customers/name", 200, 0, true));
        assertThat(merged.sort()).containsExactly(new SortState("customers/name", SortDirection.ASCENDING, 0));
    }

    @Test
    void rejectsNegativeSchemaVersion() {
        assertThatThrownBy(() -> new TableState(
                -1,
                "customers",
                List.of(new ColumnState("customers/name", 160, 0, true)),
                List.of()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("schemaVersion");
    }

    @Test
    void mergePreservesKnownStoredStateAndAppendsNewColumns() {
        TableState stored = new TableState(
                "customers",
                List.of(
                        new ColumnState("legacy/removed", 90, 0, true),
                        new ColumnState("customers/orders", 12, 1, false),
                        new ColumnState("customers/name", 500, 2, true)
                ),
                List.of(
                        new SortState("legacy/removed", SortDirection.ASCENDING, 0),
                        new SortState("customers/orders", SortDirection.DESCENDING, 1)
                )
        );

        TableState merged = TableState.merge(customerTable(), stored);

        assertThat(merged.columns()).containsExactly(
                new ColumnState("customers/orders", 40, 0, false),
                new ColumnState("customers/name", 320, 1, true),
                new ColumnState("customers/status", 120, 2, false)
        );
        assertThat(merged.sort()).containsExactly(
                new SortState("customers/orders", SortDirection.DESCENDING, 0)
        );
    }

    @Test
    void missingOrDifferentTableStateFallsBackToDefaults() {
        TableState otherTable = new TableState(
                "other",
                List.of(new ColumnState("customers/name", 200, 0, true)),
                List.of()
        );

        assertThat(TableState.merge(customerTable(), null)).isEqualTo(TableState.defaults(customerTable()));
        assertThat(TableState.merge(customerTable(), otherTable)).isEqualTo(TableState.defaults(customerTable()));
    }

    @Test
    void rejectsDuplicateColumnStateAndDescriptorPersistenceIds() {
        assertThatThrownBy(() -> new TableState(
                "customers",
                List.of(
                        new ColumnState("customers/name", 160, 0, true),
                        new ColumnState("customers/name", 160, 1, true)
                ),
                List.of()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate");

        assertThatThrownBy(() -> TableState.defaults(duplicatePersistenceTable()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate descriptor persistence id");
    }

    private static TableDescriptor<CustomerRow> customerTable() {
        return new TableDescriptor<>(
                TableKey.of("customers", CustomerRow.class),
                List.of(
                        column("name", "customers/name", 160, 80, 320, 0, true),
                        column("orders", "customers/orders", 80, 40, 200, 1, true),
                        column("status", "customers/status", 120, 60, 240, 2, false)
                )
        );
    }

    private static TableDescriptor<CustomerRow> duplicatePersistenceTable() {
        return new TableDescriptor<>(
                TableKey.of("customers", CustomerRow.class),
                List.of(
                        column("name", "customers/name", 160, 80, 320, 0, true),
                        column("displayName", "customers/name", 180, 80, 320, 1, true)
                )
        );
    }

    private static Column<CustomerRow, String> column(
            String keyId,
            String persistenceId,
            int width,
            int minWidth,
            int maxWidth,
            int order,
            boolean visible
    ) {
        return Column.readOnly(
                new ColumnDescriptor<>(
                        ColumnKey.of(keyId, CustomerRow.class, String.class),
                        ResourceKey.of(keyId + "/header", String.class),
                        null,
                        null,
                        ColumnPersistence.of(persistenceId, minWidth, maxWidth),
                        new ColumnDefaults(width, order, visible),
                        ColumnCapabilities.readOnly()
                ),
                row -> switch (keyId) {
                    case "orders" -> Integer.toString(row.orders());
                    case "status" -> row.status();
                    default -> row.name();
                }
        );
    }

    private record CustomerRow(String name, int orders, String status) {
    }
}
