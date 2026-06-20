package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.collection.MappedReadableCollection;
import cz.auderis.corusco.core.collection.ObservableSortedSet;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnCapabilities;
import cz.auderis.corusco.core.table.ColumnDefaults;
import cz.auderis.corusco.core.table.ColumnDescriptor;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableKey;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservableTableModelTest {

    @Test
    void exposesRowsAndTypedColumns() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = ObservableArrayList.of(List.of(new CustomerRow("Acme", 2)));
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());

            assertThat(model.getRowCount()).isOne();
            assertThat(model.getColumnCount()).isEqualTo(2);
            assertThat(model.getColumnName(0)).isEqualTo("customers.name");
            assertThat(model.getColumnClass(1)).isEqualTo(Integer.class);
            assertThat(model.getValueAt(0, 0)).isEqualTo("Acme");
            assertThat(model.getValueAt(0, 1)).isEqualTo(2);
            assertThat(model.isCellEditable(0, 0)).isTrue();
            assertThat(model.isCellEditable(0, 1)).isFalse();
            model.close();
        });
    }

    @Test
    void translatesSourceListChangesToTableEvents() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = ObservableArrayList.of(List.of(
                    new CustomerRow("Acme", 2),
                    new CustomerRow("Globex", 5)
            ));
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            List<EventRecord> events = recordEvents(model);

            rows.add(1, new CustomerRow("Initech", 1));
            rows.set(0, new CustomerRow("Acme Corp", 3));
            rows.remove(2);
            rows.clear();

            assertThat(events).containsExactly(
                    new EventRecord(TableModelEvent.INSERT, 1, 1, TableModelEvent.ALL_COLUMNS),
                    new EventRecord(TableModelEvent.UPDATE, 0, 0, TableModelEvent.ALL_COLUMNS),
                    new EventRecord(TableModelEvent.DELETE, 2, 2, TableModelEvent.ALL_COLUMNS),
                    new EventRecord(TableModelEvent.DELETE, 0, 1, TableModelEvent.ALL_COLUMNS)
            );
            model.close();
        });
    }

    @Test
    void editableCellsReplaceRowsThroughColumnUpdater() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = ObservableArrayList.of(List.of(new CustomerRow("Acme", 2)));
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            List<EventRecord> events = recordEvents(model);

            model.setValueAt("Acme Corp", 0, 0);

            assertThat(rows.get(0)).isEqualTo(new CustomerRow("Acme Corp", 2));
            assertThat(events).containsExactly(
                    new EventRecord(TableModelEvent.UPDATE, 0, 0, 0)
            );
            model.close();
        });
    }

    @Test
    void readOnlyModelAcceptsReadableRowsAndIgnoresEditing() {
        SwingEdt.runAndWait(() -> {
            ObservableSortedSet<CustomerRow> rows = ObservableSortedSet.of(
                    List.of(new CustomerRow("Globex", 5), new CustomerRow("Acme", 2)),
                    Comparator.comparing(CustomerRow::name)
            );
            MappedReadableCollection<CustomerRow, CustomerRow> mapped =
                    MappedReadableCollection.of(rows, row -> row);
            ObservableTableModel<CustomerRow> model = ObservableTableModel.readOnly(mapped, customerTable());
            List<EventRecord> events = recordEvents(model);

            rows.add(new CustomerRow("Initech", 1));
            model.setValueAt("Ignored", 0, 0);

            assertThat(model.getRowCount()).isEqualTo(3);
            assertThat(model.getValueAt(0, 0)).isEqualTo("Acme");
            assertThat(model.isCellEditable(0, 0)).isFalse();
            assertThat(mapped.get(0)).isEqualTo(new CustomerRow("Acme", 2));
            assertThat(events).containsExactly(
                    new EventRecord(TableModelEvent.INSERT, 2, 2, TableModelEvent.ALL_COLUMNS)
            );
            model.close();
            mapped.close();
        });
    }

    @Test
    void closeStopsFutureTableEvents() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<CustomerRow> rows = ObservableArrayList.of(List.of(new CustomerRow("Acme", 2)));
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(rows, customerTable());
            List<EventRecord> events = recordEvents(model);

            model.close();
            model.close();
            rows.add(new CustomerRow("Globex", 5));

            assertThat(model.getRowCount()).isEqualTo(2);
            assertThat(events).isEmpty();
        });
    }

    @Test
    void constructionAndObservedMutationsAreEdtConfined() {
        ObservableArrayList<CustomerRow> rows = ObservableArrayList.of(List.of(new CustomerRow("Acme", 2)));
        TableDescriptor<CustomerRow> table = customerTable();
        AtomicReference<ObservableTableModel<CustomerRow>> model = new AtomicReference<>();

        assertThatThrownBy(() -> ObservableTableModel.of(rows, table))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EDT");

        SwingEdt.runAndWait(() -> model.set(ObservableTableModel.of(rows, table)));
        assertThatThrownBy(() -> rows.add(new CustomerRow("wrong-thread", 0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EDT");
        SwingEdt.runAndWait(() -> model.get().close());
    }

    private static List<EventRecord> recordEvents(ObservableTableModel<?> model) {
        List<EventRecord> events = new ArrayList<>();
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent event) {
                events.add(EventRecord.from(event));
            }
        });
        return events;
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

    private record EventRecord(int type, int firstRow, int lastRow, int column) {

        static EventRecord from(TableModelEvent event) {
            return new EventRecord(event.getType(), event.getFirstRow(), event.getLastRow(), event.getColumn());
        }
    }
}
