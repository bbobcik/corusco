package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnCapabilities;
import cz.auderis.corusco.core.table.ColumnDefaults;
import cz.auderis.corusco.core.table.ColumnDescriptor;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.core.table.ColumnPersistence;
import cz.auderis.corusco.core.table.InMemoryTableStateStore;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableKey;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TableHeaderColumnVisibilityMenuTest {

    @Test
    void menuReflectsDescriptorColumnsAndCurrentVisibility() {
        SwingEdt.runAndWait(() -> {
            Fixture fixture = fixture();
            fixture.controller.setColumnVisible("customers/orders", false);
            fixture.controller.flushPendingSaves();

            JPopupMenu menu = fixture.menu.createMenu();

            assertThat(menu.getComponentCount()).isEqualTo(3);
            assertItem(item(menu, 0), "customers.name", "customers/name", true, true);
            assertItem(item(menu, 1), "customers.orders", "customers/orders", false, true);
            assertItem(item(menu, 2), "customers.status", "customers/status", true, true);
            fixture.close();
        });
    }

    @Test
    void togglingMenuItemDelegatesVisibilityChangeToController() {
        SwingEdt.runAndWait(() -> {
            Fixture fixture = fixture();
            JPopupMenu menu = fixture.menu.createMenu();

            item(menu, 1).doClick();
            fixture.controller.flushPendingSaves();

            assertThat(visibleColumnIds(fixture.table)).containsExactly("customers/name", "customers/status");
            assertThat(fixture.store.load("customers").orElseThrow().columns())
                    .filteredOn(column -> column.id().equals("customers/orders"))
                    .singleElement()
                    .satisfies(column -> assertThat(column.visible()).isFalse());
            fixture.close();
        });
    }

    @Test
    void lastVisibleColumnItemIsDisabled() {
        SwingEdt.runAndWait(() -> {
            Fixture fixture = fixture();
            fixture.controller.setColumnVisible("customers/orders", false);
            fixture.controller.setColumnVisible("customers/status", false);
            fixture.controller.flushPendingSaves();

            JPopupMenu menu = fixture.menu.createMenu();

            assertItem(item(menu, 0), "customers.name", "customers/name", true, false);
            assertItem(item(menu, 1), "customers.orders", "customers/orders", false, true);
            assertItem(item(menu, 2), "customers.status", "customers/status", false, true);
            fixture.close();
        });
    }

    @Test
    void closeRemovesHeaderMouseListenerAndRejectsNewMenus() {
        SwingEdt.runAndWait(() -> {
            Fixture fixture = fixture();
            MouseListener[] before = fixture.table.getTableHeader().getMouseListeners();

            fixture.menu.close();

            assertThat(fixture.table.getTableHeader().getMouseListeners()).hasSize(before.length - 1);
            assertThatThrownBy(fixture.menu::createMenu)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");
            fixture.controller.close();
            fixture.model.close();
        });
    }

    private static Fixture fixture() {
        InMemoryTableStateStore store = new InMemoryTableStateStore();
        ObservableTableModel<CustomerRow> model = ObservableTableModel.of(
                ObservableArrayList.of(List.of(new CustomerRow("Acme", 2, "active"))),
                customerTable()
        );
        JTable table = new JTable(model);
        TableStateController<CustomerRow> controller = TableStateController.install(table, model, store, 10_000);
        TableHeaderColumnVisibilityMenu<CustomerRow> menu =
                TableHeaderColumnVisibilityMenu.install(table, model, controller);
        return new Fixture(store, model, table, controller, menu);
    }

    private static JCheckBoxMenuItem item(JPopupMenu menu, int index) {
        return (JCheckBoxMenuItem) menu.getComponent(index);
    }

    private static void assertItem(
            JCheckBoxMenuItem item,
            String text,
            String columnId,
            boolean selected,
            boolean enabled
    ) {
        assertThat(item.getText()).isEqualTo(text);
        assertThat(item.getClientProperty("corusco.columnId")).isEqualTo(columnId);
        assertThat(item.isSelected()).isEqualTo(selected);
        assertThat(item.isEnabled()).isEqualTo(enabled);
    }

    private static List<String> visibleColumnIds(JTable table) {
        ObservableTableModel<?> model = (ObservableTableModel<?>) table.getModel();
        return java.util.stream.IntStream.range(0, table.getColumnModel().getColumnCount())
                .mapToObj(table.getColumnModel()::getColumn)
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

    private record Fixture(
            InMemoryTableStateStore store,
            ObservableTableModel<CustomerRow> model,
            JTable table,
            TableStateController<CustomerRow> controller,
            TableHeaderColumnVisibilityMenu<CustomerRow> menu
    ) {

        void close() {
            menu.close();
            controller.close();
            model.close();
        }
    }
}
