package cz.auderis.corusco.examples.testing;

import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.swing.testing.SwingComponentKeys;
import cz.auderis.corusco.swing.testing.SwingMvpTester;

import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Demonstrates tester table-selection helpers.
 *
 * <p>The example verifies table selection behavior using the Swing MVP tester
 * support. It is a headless table-testing scenario rather than a table-model
 * reference implementation.</p>
 */
public final class SwingMvpTesterTableExample {

    private static final ComponentKey<JTable> CUSTOMER_TABLE =
            ComponentKey.of("customer/table", JTable.class);

    private SwingMvpTesterTableExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a sorted-table selection scenario.
     *
     * @return diagnostics describing selected view/model rows
     */
    public static List<String> runScenario() {
        SwingMvpTester<CustomerTableView, Void> tester = SwingMvpTester.create(CustomerTableView::new);

        // View rows reflect the current sorter. Selecting view row 0 therefore
        // picks the alphabetically first customer, not the first model row.
        tester.selectTableViewRow(CUSTOMER_TABLE, 0)
                .assertSelectedTableViewRow(CUSTOMER_TABLE, 0)
                .assertSelectedTableModelRow(CUSTOMER_TABLE, 2);

        String selectedFromView = tester.queryOnEdt((view, presenter) ->
                view.table.getValueAt(view.table.getSelectedRow(), 0).toString());

        // Model-row selection is often better for presenter tests because the
        // row source order is stable even when the table is sorted.
        tester.selectTableModelRow(CUSTOMER_TABLE, 1)
                .assertSelectedTableModelRow(CUSTOMER_TABLE, 1);

        return tester.queryOnEdt((view, presenter) -> List.of(
                selectedFromView,
                Integer.toString(view.table.getSelectedRow()),
                Integer.toString(view.table.convertRowIndexToModel(view.table.getSelectedRow()))
        ));
    }

    private static final class CustomerTableView extends JPanel {

        private static final long serialVersionUID = 1L;

        private final JTable table = SwingComponentKeys.mark(new JTable(model()), CUSTOMER_TABLE);

        private CustomerTableView() {
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
            table.setRowSorter(sorter);
            sorter.toggleSortOrder(0);
            add(table);
        }
    }

    private static DefaultTableModel model() {
        return new DefaultTableModel(
                new Object[][] {
                        { "Carol" },
                        { "Bob" },
                        { "Alice" }
                },
                new Object[] { "Name" }
        );
    }
}
