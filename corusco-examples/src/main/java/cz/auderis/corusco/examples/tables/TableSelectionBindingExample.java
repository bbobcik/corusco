package cz.auderis.corusco.examples.tables;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnCapabilities;
import cz.auderis.corusco.core.table.ColumnDefaults;
import cz.auderis.corusco.core.table.ColumnDescriptor;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableKey;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import cz.auderis.corusco.swing.table.TableSelectionBinding;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

/**
 * Demonstrates binding sorted JTable selection to Corusco values.
 *
 * <p>The example keeps selected row state in a Corusco value while the JTable
 * may be sorted or filtered. It focuses on converting between view rows and
 * model rows so presenter state is not tied to transient table ordering.</p>
 */
public final class TableSelectionBindingExample {

    private TableSelectionBindingExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a sorted table selection scenario.
     *
     * @return selection diagnostics
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = ObservableArrayList.of(List.of(
                    new CustomerRow("Charlie", 1),
                    new CustomerRow("Alice", 3),
                    new CustomerRow("Bravo", 2)
            ));
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            JTable table = sortedTable(model);
            SimpleValue<Integer> selectedModelRow = SimpleValue.empty();
            SimpleValue<CustomerRow> selectedRow = SimpleValue.empty();
            TableSelectionBinding<CustomerRow> binding =
                    TableSelectionBinding.bind(table, model, selectedModelRow, selectedRow);

            // The first visible row is Alice after sorting, but the binding
            // records her source/model index so presenter code stays stable.
            table.getSelectionModel().setSelectionInterval(0, 0);
            result.add(selectedModelRow.value() + ":" + selectedRow.value().name());

            // Presenter code can also drive selection through the model index;
            // JTable converts it back to the current sorted view row.
            selectedModelRow.setValue(2);
            result.add(table.getSelectedRow() + ":" + selectedRow.value().name());

            // Close with the view lifecycle to remove Swing and value listeners.
            binding.close();
            model.close();
        });
        return List.copyOf(result);
    }

    private static JTable sortedTable(ObservableTableModel<CustomerRow> model) {
        JTable table = new JTable(model);
        TableRowSorter<ObservableTableModel<CustomerRow>> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        sorter.sort();
        return table;
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
