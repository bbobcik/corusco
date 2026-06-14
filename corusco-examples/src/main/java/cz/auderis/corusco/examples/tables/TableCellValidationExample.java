package cz.auderis.corusco.examples.tables;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnCapabilities;
import cz.auderis.corusco.core.table.ColumnDefaults;
import cz.auderis.corusco.core.table.ColumnDescriptor;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.core.table.TableCellProblems;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableKey;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import cz.auderis.corusco.swing.table.TableCellValidationBinding;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Demonstrates typed table cell problem decoration.
 *
 * <p>The example targets validation problems at table cells and decorates the
 * matching Swing table locations. It shows how typed row and column identities
 * let table feedback stay independent from view indices.</p>
 */
public final class TableCellValidationExample {

    private static final ProblemCode REQUIRED = ProblemCode.of("required");
    private static final ColumnKey<CustomerRow, String> NAME =
            ColumnKey.of("name", CustomerRow.class, String.class);
    private static final ColumnKey<CustomerRow, Integer> ORDERS =
            ColumnKey.of("orders", CustomerRow.class, Integer.class);

    private TableCellValidationExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a table cell validation decoration scenario.
     *
     * @return diagnostics from rendered cells
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = ObservableArrayList.of(List.of(
                    new CustomerRow("Alice", 0),
                    new CustomerRow("", 2)
            ));
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            JTable table = new JTable(model);

            // Cell problems target the row plus a typed ColumnKey. There is no
            // JavaBeans property path or visible column-name string involved.
            SimpleValue<ProblemSet> problems = SimpleValue.of(ProblemSet.of(Problem.validation(
                    REQUIRED,
                    ProblemSeverity.ERROR,
                    TableCellProblems.target(rows.get(1), NAME),
                    "Customer name is required"
            )));
            TableCellValidationBinding<CustomerRow> binding =
                    TableCellValidationBinding.bind(table, model, problems);

            // Rendering asks the binding to translate the view cell back to
            // model row/column before it looks up the matching problem.
            result.add(render(table, 1, 0).getToolTipText());
            result.add(render(table, 0, 1).getToolTipText() == null ? "no-problem" : "problem");

            // Close with the table lifecycle to restore renderers and detach
            // the problem-value subscription.
            binding.close();
            model.close();
        });
        return List.copyOf(result);
    }

    private static JComponent render(JTable table, int row, int column) {
        TableCellRenderer renderer = table.getCellRenderer(row, column);
        Component component = renderer.getTableCellRendererComponent(
                table,
                table.getValueAt(row, column),
                false,
                false,
                row,
                column
        );
        return (JComponent) component;
    }

    private static TableDescriptor<CustomerRow> customerTable() {
        Column<CustomerRow, String> name = Column.readOnly(
                new ColumnDescriptor<>(
                        NAME,
                        ResourceKey.of("customers.name", String.class),
                        null,
                        ColumnDefaults.visible(160, 0),
                        ColumnCapabilities.readOnly()
                ),
                CustomerRow::name
        );
        Column<CustomerRow, Integer> orders = Column.readOnly(
                new ColumnDescriptor<>(
                        ORDERS,
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
