package cz.auderis.corusco.core.data;

import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnCapabilities;
import cz.auderis.corusco.core.table.ColumnDefaults;
import cz.auderis.corusco.core.table.ColumnDescriptor;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.core.table.ColumnPersistence;
import cz.auderis.corusco.core.table.ColumnState;
import cz.auderis.corusco.core.table.SortDirection;
import cz.auderis.corusco.core.table.SortState;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableKey;
import cz.auderis.corusco.core.table.TableState;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoruscoTableDataQueriesTest {

    @Test
    void descriptorRequiresMatchingRowIdentity() {
        TableDescriptor<Row> table = table();

        CoruscoTableDataDescriptor<Row, Long> descriptor = new CoruscoTableDataDescriptor<>(
                table,
                CoruscoRowIdentity.of(Row.class, Long.class, Row::id)
        );

        assertThat(descriptor.table()).isSameAs(table);
        @SuppressWarnings({"rawtypes", "unchecked"})
        CoruscoRowIdentity<Row, Long> wrongIdentity = (CoruscoRowIdentity) CoruscoRowIdentity.of(
                OtherRow.class,
                Long.class,
                OtherRow::id
        );
        assertThatThrownBy(() -> new CoruscoTableDataDescriptor<>(
                table,
                wrongIdentity
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("row type");
    }

    @Test
    void tableStateSortMapsToDataQueryUsingPersistenceIds() {
        TableState state = new TableState(
                "rows",
                List.of(
                        new ColumnState("rows/name", 100, 0, true),
                        new ColumnState("rows/id", 80, 1, true)
                ),
                List.of(
                        new SortState("unknown", SortDirection.ASCENDING, 0),
                        new SortState("rows/name", SortDirection.DESCENDING, 1),
                        new SortState("rows/id", SortDirection.ASCENDING, 2)
                )
        );

        CoruscoDataQuery query = CoruscoTableDataQueries.fromTableState(
                table(),
                state,
                List.of(new CoruscoDataFilter("status", CoruscoDataFilterOperator.EQUALS, List.of("active")))
        );

        assertThat(query.filters()).containsExactly(
                new CoruscoDataFilter("status", CoruscoDataFilterOperator.EQUALS, List.of("active"))
        );
        assertThat(query.sort()).containsExactly(
                new CoruscoDataSort("rows/name", CoruscoDataSort.Direction.DESCENDING, 0),
                new CoruscoDataSort("rows/id", CoruscoDataSort.Direction.ASCENDING, 1)
        );
    }

    private static TableDescriptor<Row> table() {
        return new TableDescriptor<>(
                TableKey.of("rows", Row.class),
                List.of(
                        column("id", "rows/id", Long.class),
                        column("name", "rows/name", String.class)
                )
        );
    }

    private static <V> Column<Row, V> column(String id, String persistenceId, Class<V> valueType) {
        return Column.readOnly(
                new ColumnDescriptor<>(
                        ColumnKey.of(id, Row.class, valueType),
                        ResourceKey.of(id + "/header", String.class),
                        null,
                        null,
                        ColumnPersistence.of(persistenceId, 40, 400),
                        new ColumnDefaults(120, 0, true),
                        ColumnCapabilities.readOnly()
                ),
                row -> valueType.cast(id.equals("id") ? row.id() : row.name())
        );
    }

    private record Row(long id, String name) {
    }

    private record OtherRow(long id) {
    }
}
