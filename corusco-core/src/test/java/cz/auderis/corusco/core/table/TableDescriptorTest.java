package cz.auderis.corusco.core.table;

import cz.auderis.corusco.core.key.ResourceKey;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TableDescriptorTest {

    @Test
    void keysValidateStableIdsAndTypes() {
        TableKey<CustomerRow> tableKey = TableKey.of("customers", CustomerRow.class);
        ColumnKey<CustomerRow, String> columnKey = ColumnKey.of("name", CustomerRow.class, String.class);

        assertThat(tableKey.id()).isEqualTo("customers");
        assertThat(tableKey.rowType()).isEqualTo(CustomerRow.class);
        assertThat(columnKey.id()).isEqualTo("name");
        assertThat(columnKey.valueType()).isEqualTo(String.class);
        assertThat(tableKey).hasToString("TableKey[CustomerRow#customers]");
        assertThat(columnKey).hasToString("ColumnKey[CustomerRow#name:String]");
        assertThatThrownBy(() -> TableKey.of(" ", CustomerRow.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
        assertThatThrownBy(() -> ColumnKey.of("orders", CustomerRow.class, int.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valueType");
    }

    @Test
    void descriptorsAreImmutableAndValidateDefaults() {
        Column<CustomerRow, String> name = readOnlyNameColumn();
        TableDescriptor<CustomerRow> descriptor = new TableDescriptor<>(
                TableKey.of("customers", CustomerRow.class),
                List.of(name)
        );

        assertThat(descriptor.columns()).containsExactly(name);
        assertThatThrownBy(() -> descriptor.columns().add(name))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ColumnDefaults.visible(0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width");
        assertThatThrownBy(() -> new TableDescriptor<>(TableKey.of("customers", CustomerRow.class), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("columns");
    }

    @Test
    void columnReadsAndUpdatesRecordRowsThroughExplicitFunctions() {
        Column<CustomerRow, String> name = editableNameColumn();
        CustomerRow original = new CustomerRow("Acme", 7);

        CustomerRow updated = name.update(original, "Globex");

        assertThat(name.value(original)).isEqualTo("Acme");
        assertThat(updated).isEqualTo(new CustomerRow("Globex", 7));
    }

    @Test
    void readOnlyColumnsRejectUpdatesAndEditableColumnsRequireUpdaters() {
        Column<CustomerRow, String> readOnly = readOnlyNameColumn();
        ColumnDescriptor<CustomerRow, String> editableDescriptor = nameDescriptor(ColumnCapabilities.editableColumn());

        assertThat(readOnly.editable()).isFalse();
        assertThatThrownBy(() -> readOnly.update(new CustomerRow("Acme", 1), "Globex"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("name");
        assertThatThrownBy(() -> Column.readOnly(editableDescriptor, CustomerRow::name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("editable");
    }

    private static Column<CustomerRow, String> readOnlyNameColumn() {
        return Column.readOnly(nameDescriptor(ColumnCapabilities.readOnly()), CustomerRow::name);
    }

    private static Column<CustomerRow, String> editableNameColumn() {
        return Column.editable(
                nameDescriptor(ColumnCapabilities.editableColumn()),
                CustomerRow::name,
                (row, value) -> new CustomerRow(value, row.orders())
        );
    }

    private static ColumnDescriptor<CustomerRow, String> nameDescriptor(ColumnCapabilities capabilities) {
        return new ColumnDescriptor<>(
                ColumnKey.of("name", CustomerRow.class, String.class),
                ResourceKey.of("customers.name", String.class),
                ResourceKey.of("customers.name.help", String.class),
                ColumnDefaults.visible(160, 0),
                capabilities
        );
    }

    private record CustomerRow(String name, int orders) {
    }
}
