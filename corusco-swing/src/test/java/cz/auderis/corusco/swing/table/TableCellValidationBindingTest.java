package cz.auderis.corusco.swing.table;

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
import java.awt.Component;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TableCellValidationBindingTest {

    private static final ProblemCode REQUIRED = ProblemCode.of("required");
    private static final ColumnKey<CustomerRow, String> NAME =
            ColumnKey.of("name", CustomerRow.class, String.class);
    private static final ColumnKey<CustomerRow, Integer> ORDERS =
            ColumnKey.of("orders", CustomerRow.class, Integer.class);

    @Test
    void decoratesMatchingCellAndLeavesOtherCellsPlain() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = rows();
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            JTable table = new JTable(model);
            SimpleValue<ProblemSet> problems = SimpleValue.of(ProblemSet.of(problem(rows.get(1), NAME, "Name required")));
            TableCellValidationBinding<CustomerRow> binding = TableCellValidationBinding.bind(table, model, problems);

            JComponent decorated = render(table, 1, 0);
            JComponent plain = render(table, 1, 1);

            assertThat(decorated.getToolTipText()).isEqualTo("Name required");
            assertThat(decorated.getBorder()).isNotNull();
            assertThat(plain.getToolTipText()).isNull();
            binding.close();
            model.close();
        });
    }

    @Test
    void usesViewModelConversionForSortedTables() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = rows();
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            JTable table = sortedTable(model);
            SimpleValue<ProblemSet> problems = SimpleValue.of(ProblemSet.of(problem(rows.get(1), NAME, "Alice problem")));
            TableCellValidationBinding<CustomerRow> binding = TableCellValidationBinding.bind(table, model, problems);

            assertThat(table.convertRowIndexToModel(0)).isEqualTo(1);
            assertThat(render(table, 0, 0).getToolTipText()).isEqualTo("Alice problem");
            binding.close();
            model.close();
        });
    }

    @Test
    void updatesDecorationWhenProblemValueChanges() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = rows();
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            JTable table = new JTable(model);
            SimpleValue<ProblemSet> problems = SimpleValue.of(ProblemSet.empty());
            TableCellValidationBinding<CustomerRow> binding = TableCellValidationBinding.bind(table, model, problems);

            assertThat(render(table, 0, 0).getToolTipText()).isNull();
            problems.setValue(ProblemSet.of(problem(rows.get(0), NAME, "Now invalid")));

            assertThat(render(table, 0, 0).getToolTipText()).isEqualTo("Now invalid");
            problems.setValue(ProblemSet.empty());
            assertThat(render(table, 0, 0).getToolTipText()).isNull();
            binding.close();
            model.close();
        });
    }

    @Test
    void closeRestoresOriginalRenderer() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = rows();
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            CountingTable table = new CountingTable(model);
            TableCellRenderer original = table.getDefaultRenderer(String.class);
            SimpleValue<ProblemSet> problems = SimpleValue.of(ProblemSet.empty());
            TableCellValidationBinding<CustomerRow> binding = TableCellValidationBinding.bind(table, model, problems);

            assertThat(table.getDefaultRenderer(String.class)).isNotSameAs(original);
            binding.close();
            table.resetRepaintCount();
            problems.setValue(ProblemSet.of(problem(rows.get(0), NAME, "After close")));

            assertThat(table.getDefaultRenderer(String.class)).isSameAs(original);
            assertThat(table.repaintCount()).isZero();
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

        assertThatThrownBy(() -> TableCellValidationBinding.bind(table.get(), model.get(), SimpleValue.of(ProblemSet.empty())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EDT");
        SwingEdt.runAndWait(() -> model.get().close());
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

    private static JTable sortedTable(ObservableTableModel<CustomerRow> model) {
        JTable table = new JTable(model);
        TableRowSorter<ObservableTableModel<CustomerRow>> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        sorter.sort();
        return table;
    }

    private static Problem problem(CustomerRow row, ColumnKey<CustomerRow, ?> column, String message) {
        return Problem.validation(
                REQUIRED,
                ProblemSeverity.ERROR,
                TableCellProblems.target(row, column),
                message
        );
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

    private static final class CountingTable extends JTable {

        private int repaintCount;

        private CountingTable(ObservableTableModel<CustomerRow> model) {
            super(model);
        }

        @Override
        public void repaint() {
            repaintCount++;
            super.repaint();
        }

        private void resetRepaintCount() {
            repaintCount = 0;
        }

        private int repaintCount() {
            return repaintCount;
        }
    }
}
