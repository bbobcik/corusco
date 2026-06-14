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
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;

/**
 * Demonstrates a typed observable table model over immutable record rows.
 *
 * <p>The scenario connects a table descriptor and observable row list to a
 * Swing table model. It is headless-safe and focuses on how row values, column
 * descriptors, and list changes become table model events.</p>
 */
public final class ObservableTableModelExample {

    private ObservableTableModelExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs an editable table model scenario.
     *
     * @return table diagnostics
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = ObservableArrayList.of(List.of(
                    new CustomerRow("Acme", 2),
                    new CustomerRow("Globex", 5)
            ));
            TableDescriptor<CustomerRow> table = customerTable();
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, table);

            // The table sees typed Column constants, not JavaBeans property
            // names. Later annotation processing should generate this shape.
            JTable jTable = new JTable(model);
            result.add(model.getColumnName(0) + ":" + model.getValueAt(0, 0));

            // Editing flows through the column updater. For record rows that
            // means replacing the row in the observable list with a new record.
            model.setValueAt("Acme Corp", 0, 0);
            result.add(rows.get(0).name());

            // Row mutations remain owned by the observable list. Swing table
            // models must be used on the EDT unless an explicit dispatcher is
            // placed in front of them.
            rows.add(new CustomerRow("Initech", 1));
            result.add(Integer.toString(jTable.getRowCount()));
            model.close();
        });
        return List.copyOf(result);
    }

    private static TableDescriptor<CustomerRow> customerTable() {
        Column<CustomerRow, String> name = Column.editable(
                new ColumnDescriptor<>(
                        ColumnKey.of("name", CustomerRow.class, String.class),
                        ResourceKey.of("customers.name", String.class),
                        null,
                        ColumnDefaults.visible(160, 0),
                        ColumnCapabilities.editableColumn()
                ),
                CustomerRow::name,
                (row, value) -> new CustomerRow(value, row.orders())
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
