package cz.auderis.corusco.examples.testing;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnCapabilities;
import cz.auderis.corusco.core.table.ColumnDefaults;
import cz.auderis.corusco.core.table.ColumnDescriptor;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.core.table.InMemoryTableStateStore;
import cz.auderis.corusco.core.table.SortDirection;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableKey;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import cz.auderis.corusco.swing.table.TableStateController;
import cz.auderis.corusco.swing.testing.SwingComponentKeys;
import cz.auderis.corusco.swing.testing.SwingMvpTester;

import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;

/**
 * Demonstrates tester table-state assertions.
 *
 * <p>The scenario exercises helper assertions for column widths, visibility,
 * and sort state. It is intended for tests that need to verify persisted table
 * state without relying on manual JTable inspection.</p>
 */
public final class SwingMvpTesterTableStateExample {

    private static final ComponentKey<JTable> CUSTOMER_TABLE =
            ComponentKey.of("customer/table", JTable.class);

    private SwingMvpTesterTableStateExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a table-state assertion scenario.
     *
     * @return captured table-state diagnostics
     */
    public static List<String> runScenario() {
        SwingMvpTester<CustomerView, CustomerPresenter> tester = SwingMvpTester.create(
                CustomerView::new,
                CustomerPresenter::new
        );

        // Table-state assertions read the controller's public TableState
        // snapshot, so tests stay at stable table/column ids instead of Swing
        // TableColumn instances.
        tester.runOnEdt((view, presenter) -> {
                    view.table.getColumnModel().moveColumn(1, 0);
                    view.table.getColumnModel().getColumn(0).setWidth(140);
                    view.table.getRowSorter().setSortKeys(List.of(
                            new RowSorter.SortKey(1, SortOrder.DESCENDING)
                    ));
                })
                .assertTableStateId((view, presenter) -> presenter.controller.captureState(), "customers")
                .assertTableColumnOrder((view, presenter) -> presenter.controller.captureState(), "orders", 0)
                .assertTableColumnWidth((view, presenter) -> presenter.controller.captureState(), "orders", 140)
                .assertTableSort((view, presenter) -> presenter.controller.captureState(),
                        "orders", SortDirection.DESCENDING, 0);

        List<String> result = tester.queryOnEdt((view, presenter) -> {
            var state = presenter.controller.captureState();
            return List.of(
                    state.columns().getFirst().id(),
                    Integer.toString(state.columns().getFirst().width()),
                    state.sort().getFirst().columnId()
            );
        });

        // The presenter owns the table model/controller lifecycle; the tester
        // just drives the view and assertions around that lifecycle.
        tester.runOnEdt((view, presenter) -> presenter.close());
        return result;
    }

    private static final class CustomerView extends JPanel {

        private static final long serialVersionUID = 1L;

        private final JTable table = SwingComponentKeys.mark(new JTable(), CUSTOMER_TABLE);

        private CustomerView() {
            add(table);
        }
    }

    private static final class CustomerPresenter {

        private final ObservableTableModel<CustomerRow> model;
        private final TableStateController<CustomerRow> controller;

        private CustomerPresenter(CustomerView view) {
            model = ObservableTableModel.of(
                    ObservableArrayList.of(List.of(
                            new CustomerRow("Acme", 2),
                            new CustomerRow("Globex", 5)
                    )),
                    customerTable()
            );
            view.table.setModel(model);
            view.table.setAutoCreateRowSorter(true);
            controller = TableStateController.install(
                    view.table,
                    model,
                    new InMemoryTableStateStore(),
                    0
            );
        }

        private void close() {
            controller.close();
            model.close();
        }
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
