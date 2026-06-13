package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnCapabilities;
import cz.auderis.corusco.core.table.ColumnDefaults;
import cz.auderis.corusco.core.table.ColumnDescriptor;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.core.table.ColumnPersistence;
import cz.auderis.corusco.core.table.ColumnState;
import cz.auderis.corusco.core.table.InMemoryTableStateStore;
import cz.auderis.corusco.core.table.SortDirection;
import cz.auderis.corusco.core.table.SortState;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableKey;
import cz.auderis.corusco.core.table.TableState;
import cz.auderis.corusco.core.table.TableStateStore;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.List;
import java.util.Optional;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableColumnModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableStateControllerTest {

    @Test
    void restoresStoredColumnLayoutAndSortState() {
        SwingEdt.runAndWait(() -> {
            InMemoryTableStateStore store = new InMemoryTableStateStore();
            store.save(new TableState(
                    "customers",
                    List.of(
                            new ColumnState("customers/orders", 20, 0, true),
                            new ColumnState("customers/name", 500, 1, true),
                            new ColumnState("customers/status", 120, 2, false)
                    ),
                    List.of(new SortState("customers/orders", SortDirection.DESCENDING, 0))
            ));
            ObservableTableModel<CustomerRow> model = model();
            JTable table = table(model);

            TableStateController<CustomerRow> controller = TableStateController.install(table, model, store);

            assertThat(visibleColumnIds(table)).containsExactly("customers/orders", "customers/name");
            assertThat(table.getColumnModel().getColumn(0).getWidth()).isEqualTo(40);
            assertThat(table.getColumnModel().getColumn(1).getWidth()).isEqualTo(320);
            List<? extends RowSorter.SortKey> sortKeys = table.getRowSorter().getSortKeys();
            assertThat(sortKeys).hasSize(1);
            assertThat(sortKeys.getFirst().getColumn()).isEqualTo(1);
            assertThat(sortKeys.getFirst().getSortOrder()).isEqualTo(SortOrder.DESCENDING);
            assertThat(controller.captureState().columns()).containsExactly(
                    new ColumnState("customers/orders", 40, 0, true),
                    new ColumnState("customers/name", 320, 1, true),
                    new ColumnState("customers/status", 120, 2, false)
            );
            controller.close();
            model.close();
        });
    }

    @Test
    void persistsLayoutAcrossTableRecreation() {
        SwingEdt.runAndWait(() -> {
            InMemoryTableStateStore store = new InMemoryTableStateStore();
            ObservableTableModel<CustomerRow> firstModel = model();
            JTable firstTable = table(firstModel);
            TableStateController<CustomerRow> first = TableStateController.install(firstTable, firstModel, store);

            firstTable.getColumnModel().moveColumn(1, 0);
            firstTable.getColumnModel().getColumn(0).setWidth(150);
            first.setColumnVisible("customers/status", false);
            first.saveNow();
            first.close();
            firstModel.close();

            ObservableTableModel<CustomerRow> secondModel = model();
            JTable secondTable = table(secondModel);
            TableStateController<CustomerRow> second = TableStateController.install(secondTable, secondModel, store);

            assertThat(visibleColumnIds(secondTable)).containsExactly("customers/orders", "customers/name");
            assertThat(secondTable.getColumnModel().getColumn(0).getWidth()).isEqualTo(150);
            second.close();
            secondModel.close();
        });
    }

    @Test
    void programmaticVisibilityToggleSavesState() {
        SwingEdt.runAndWait(() -> {
            InMemoryTableStateStore store = new InMemoryTableStateStore();
            ObservableTableModel<CustomerRow> model = model();
            JTable table = table(model);
            TableStateController<CustomerRow> controller = TableStateController.install(table, model, store);

            controller.setColumnVisible("customers/orders", false);
            controller.flushPendingSaves();

            assertThat(visibleColumnIds(table)).containsExactly("customers/name", "customers/status");
            assertThat(store.load("customers").orElseThrow().columns())
                    .filteredOn(column -> column.id().equals("customers/orders"))
                    .containsExactly(new ColumnState("customers/orders", 80, 2, false));
            controller.close();
            model.close();
        });
    }

    @Test
    void eventSavesAreDebouncedUntilFlushed() {
        SwingEdt.runAndWait(() -> {
            RecordingStore store = new RecordingStore();
            ObservableTableModel<CustomerRow> model = model();
            JTable table = table(model);
            TableStateController<CustomerRow> controller = TableStateController.install(table, model, store, 10_000);
            assertThat(store.saveCount()).isEqualTo(1);

            table.getColumnModel().moveColumn(1, 0);
            table.getColumnModel().getColumn(0).setWidth(150);
            table.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));

            assertThat(store.saveCount()).isEqualTo(1);
            controller.flushPendingSaves();

            assertThat(store.saveCount()).isEqualTo(2);
            assertThat(store.state().orElseThrow().columns().getFirst().id()).isEqualTo("customers/orders");
            assertThat(store.state().orElseThrow().sort()).containsExactly(
                    new SortState("customers/name", SortDirection.ASCENDING, 0)
            );
            controller.close();
            model.close();
        });
    }

    @Test
    void explicitSaveNowCancelsPendingDelayedSave() {
        SwingEdt.runAndWait(() -> {
            RecordingStore store = new RecordingStore();
            ObservableTableModel<CustomerRow> model = model();
            JTable table = table(model);
            TableStateController<CustomerRow> controller = TableStateController.install(table, model, store, 10_000);

            table.getColumnModel().moveColumn(1, 0);
            controller.saveNow();
            controller.flushPendingSaves();

            assertThat(store.saveCount()).isEqualTo(2);
            assertThat(store.state().orElseThrow().columns().getFirst().id()).isEqualTo("customers/orders");
            controller.close();
            model.close();
        });
    }

    @Test
    void capturesSortChangesAndFlushesOnClose() {
        SwingEdt.runAndWait(() -> {
            RecordingStore store = new RecordingStore();
            ObservableTableModel<CustomerRow> model = model();
            JTable table = table(model);
            TableStateController<CustomerRow> controller = TableStateController.install(table, model, store);

            table.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
            controller.close();

            assertThat(store.state().orElseThrow().sort()).containsExactly(
                    new SortState("customers/name", SortDirection.ASCENDING, 0)
            );
            assertThat(store.flushed()).isTrue();
            model.close();
        });
    }

    private static ObservableTableModel<CustomerRow> model() {
        return ObservableTableModel.of(
                ObservableArrayList.of(List.of(
                        new CustomerRow("Acme", 2, "active"),
                        new CustomerRow("Globex", 5, "paused")
                )),
                customerTable()
        );
    }

    private static JTable table(ObservableTableModel<CustomerRow> model) {
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        return table;
    }

    private static List<String> visibleColumnIds(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        ObservableTableModel<?> model = (ObservableTableModel<?>) table.getModel();
        return java.util.stream.IntStream.range(0, columnModel.getColumnCount())
                .mapToObj(columnModel::getColumn)
                .map(column -> model.descriptor().column(column.getModelIndex()).descriptor().persistence().id())
                .toList();
    }

    private static TableDescriptor<CustomerRow> customerTable() {
        return new TableDescriptor<>(
                TableKey.of("customers", CustomerRow.class),
                List.of(
                        column("name", String.class, "customers/name", 160, 80, 320, 0),
                        column("orders", Integer.class, "customers/orders", 80, 40, 200, 1),
                        column("status", String.class, "customers/status", 120, 60, 240, 2)
                )
        );
    }

    private static <V> Column<CustomerRow, V> column(
            String keyId,
            Class<V> valueType,
            String persistenceId,
            int width,
            int minWidth,
            int maxWidth,
            int order
    ) {
        return Column.readOnly(
                new ColumnDescriptor<>(
                        ColumnKey.of(keyId, CustomerRow.class, valueType),
                        ResourceKey.of("customers." + keyId, String.class),
                        null,
                        null,
                        ColumnPersistence.of(persistenceId, minWidth, maxWidth),
                        ColumnDefaults.visible(width, order),
                        ColumnCapabilities.readOnly()
                ),
                row -> valueType.cast(switch (keyId) {
                    case "orders" -> row.orders();
                    case "status" -> row.status();
                    default -> row.name();
                })
        );
    }

    private record CustomerRow(String name, int orders, String status) {
    }

    private static final class RecordingStore implements TableStateStore {

        private final InMemoryTableStateStore delegate = new InMemoryTableStateStore();
        private boolean flushed;
        private int saveCount;

        @Override
        public Optional<TableState> load(String tableId) {
            return delegate.load(tableId);
        }

        @Override
        public void save(TableState state) {
            saveCount++;
            delegate.save(state);
        }

        @Override
        public void remove(String tableId) {
            delegate.remove(tableId);
        }

        @Override
        public void flush() {
            flushed = true;
            delegate.flush();
        }

        Optional<TableState> state() {
            return delegate.load("customers");
        }

        boolean flushed() {
            return flushed;
        }

        int saveCount() {
            return saveCount;
        }
    }
}
