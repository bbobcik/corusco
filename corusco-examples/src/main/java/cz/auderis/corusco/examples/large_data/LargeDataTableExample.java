package cz.auderis.corusco.examples.large_data;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.examples.book.BookExampleSupport;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import net.miginfocom.swing.MigLayout;

public final class LargeDataTableExample {

    public record CustomerRow(int id, String name, String state) {
    }

    private LargeDataTableExample() {
        throw new AssertionError("No instances");
    }

    public static JInternalFrame createWindow() {
        return BookExampleSupport.frame("Large data", createContent(), new Dimension(680, 420));
    }

    public static JPanel createContent() {
        BookExampleSupport.requireEdt();
        ObservableArrayList<CustomerRow> rows = ObservableArrayList.of(sampleRows());
        JTable table = new JTable(new CustomerTableModel(rows.snapshot()));
        JPanel panel = new JPanel(new MigLayout("fill, insets 16", "[grow]", "[][grow]"));
        panel.add(new JLabel(rows.size() + " deterministic rows"), "wrap");
        panel.add(new JScrollPane(table), "grow");
        return panel;
    }

    private static List<CustomerRow> sampleRows() {
        return java.util.stream.IntStream.rangeClosed(1, 200)
                .mapToObj(i -> new CustomerRow(i, "Customer " + i, i % 3 == 0 ? "Review" : "Active"))
                .toList();
    }

    private static final class CustomerTableModel extends AbstractTableModel {
        private final List<CustomerRow> rows;

        private CustomerTableModel(List<CustomerRow> rows) {
            this.rows = rows;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Id";
                case 1 -> "Name";
                case 2 -> "State";
                default -> throw new IllegalArgumentException("column " + column);
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CustomerRow row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.id();
                case 1 -> row.name();
                case 2 -> row.state();
                default -> throw new IllegalArgumentException("column " + columnIndex);
            };
        }
    }
}
