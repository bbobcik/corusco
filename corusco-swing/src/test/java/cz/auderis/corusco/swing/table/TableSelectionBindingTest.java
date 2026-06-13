package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnCapabilities;
import cz.auderis.corusco.core.table.ColumnDefaults;
import cz.auderis.corusco.core.table.ColumnDescriptor;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableKey;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TableSelectionBindingTest {

    @Test
    void userSelectionStoresModelIndexAndRowThroughSorterConversion() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = rows();
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            JTable table = sortedTable(model);
            SimpleValue<Integer> selectedIndex = SimpleValue.empty();
            SimpleValue<CustomerRow> selectedRow = SimpleValue.empty();
            List<String> events = new ArrayList<>();
            selectedIndex.subscribe(event -> events.add(event.newValue() + ":" + event.origin()));
            TableSelectionBinding<CustomerRow> binding = TableSelectionBinding.bind(table, model, selectedIndex, selectedRow);

            table.getSelectionModel().setSelectionInterval(0, 0);

            assertThat(table.convertRowIndexToModel(0)).isEqualTo(1);
            assertThat(selectedIndex.value()).isEqualTo(1);
            assertThat(selectedRow.value()).isEqualTo(new CustomerRow("Alice", 3));
            assertThat(events).contains("1:" + ChangeOrigin.USER);
            binding.close();
            model.close();
        });
    }

    @Test
    void presenterSelectedModelIndexSelectsCorrespondingViewRow() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = rows();
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            JTable table = sortedTable(model);
            SimpleValue<Integer> selectedIndex = SimpleValue.empty();
            SimpleValue<CustomerRow> selectedRow = SimpleValue.empty();
            TableSelectionBinding<CustomerRow> binding = TableSelectionBinding.bind(table, model, selectedIndex, selectedRow);

            selectedIndex.setValue(1);

            assertThat(table.getSelectedRow()).isZero();
            assertThat(selectedRow.value()).isEqualTo(new CustomerRow("Alice", 3));
            binding.close();
            model.close();
        });
    }

    @Test
    void clearsValuesWhenSelectedModelRowNoLongerExists() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = rows();
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            JTable table = new JTable(model);
            SimpleValue<Integer> selectedIndex = SimpleValue.empty();
            SimpleValue<CustomerRow> selectedRow = SimpleValue.empty();
            TableSelectionBinding<CustomerRow> binding = TableSelectionBinding.bind(table, model, selectedIndex, selectedRow);
            selectedIndex.setValue(1);

            rows.remove(1);

            assertThat(table.getSelectedRow()).isEqualTo(-1);
            assertThat(selectedIndex.value()).isNull();
            assertThat(selectedRow.value()).isNull();
            binding.close();
            model.close();
        });
    }

    @Test
    void closeRemovesTableAndValueListeners() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = rows();
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            JTable table = new JTable(model);
            SimpleValue<Integer> selectedIndex = SimpleValue.empty();
            TableSelectionBinding<CustomerRow> binding = TableSelectionBinding.bind(table, model, selectedIndex);

            binding.close();
            binding.close();
            table.getSelectionModel().setSelectionInterval(1, 1);
            selectedIndex.setValue(0);

            assertThat(selectedIndex.value()).isZero();
            assertThat(table.getSelectedRow()).isEqualTo(1);
            model.close();
        });
    }

    @Test
    void constructionOffEdtFailsFast() {
        ObservableArrayList<CustomerRow> rows = rows();
        AtomicReference<ObservableTableModel<CustomerRow>> model = new AtomicReference<>();
        AtomicReference<JTable> table = new AtomicReference<>();
        SwingEdt.runAndWait(() -> {
            model.set(ObservableTableModel.of(rows, customerTable()));
            table.set(new JTable(model.get()));
        });

        assertThatThrownBy(() -> TableSelectionBinding.bind(table.get(), model.get(), SimpleValue.empty()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EDT");
        SwingEdt.runAndWait(() -> model.get().close());
    }

    private static JTable sortedTable(ObservableTableModel<CustomerRow> model) {
        JTable table = new JTable(model);
        TableRowSorter<ObservableTableModel<CustomerRow>> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        sorter.sort();
        return table;
    }

    private static ObservableArrayList<CustomerRow> rows() {
        return ObservableArrayList.of(List.of(
                new CustomerRow("Charlie", 1),
                new CustomerRow("Alice", 3),
                new CustomerRow("Bravo", 2)
        ));
    }

    private static TableDescriptor<CustomerRow> customerTable() {
        Column<CustomerRow, String> name = Column.readOnly(
                new ColumnDescriptor<>(
                        ColumnKey.of("name", CustomerRow.class, String.class),
                        ResourceKey.of("customers.name", String.class),
                        null,
                        ColumnDefaults.visible(160, 0),
                        ColumnCapabilities.readOnly()
                ),
                CustomerRow::name
        );
        Column<CustomerRow, Integer> orders = Column.readOnly(
                new ColumnDescriptor<>(
                        ColumnKey.of("orders", CustomerRow.class, Integer.class),
                        ResourceKey.of("customers.orders", String.class),
                        null,
                        ColumnDefaults.visible(80, 1),
                        ColumnCapabilities.readOnly()
                ),
                CustomerRow::orders
        );
        return new TableDescriptor<>(TableKey.of("customers", CustomerRow.class), List.of(name, orders));
    }

    private record CustomerRow(String name, int orders) {
    }
}
