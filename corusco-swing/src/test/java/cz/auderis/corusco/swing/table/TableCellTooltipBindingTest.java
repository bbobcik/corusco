package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnCapabilities;
import cz.auderis.corusco.core.table.ColumnDefaults;
import cz.auderis.corusco.core.table.ColumnDescriptor;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableKey;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableCellTooltipBindingTest {

    private static final ResourceKey<String> NAME_HEADER = ResourceKey.of("customers/name/header", String.class);
    private static final ResourceKey<String> NAME_TOOLTIP = ResourceKey.of("customers/name/tooltip", String.class);
    private static final ResourceKey<String> ORDERS_HEADER = ResourceKey.of("customers/orders/header", String.class);
    private static final ResourceKey<String> ORDERS_TOOLTIP = ResourceKey.of("customers/orders/tooltip", String.class);

    @Test
    void showsTooltipForHoveredBodyCell() {
        SwingEdt.runAndWait(() -> {
            Fixture fixture = fixture(Resources.of(Map.of(
                    NAME_TOOLTIP.id(), "Customer display name",
                    ORDERS_TOOLTIP.id(), "Number of open orders"
            )));

            mouseMoved(fixture.table(), 5, 5);

            assertThat(fixture.table().getToolTipText()).isEqualTo("Customer display name");
            fixture.close();
        });
    }

    @Test
    void clearsTooltipForMissingResourceAndRestoresOriginalOnClose() {
        SwingEdt.runAndWait(() -> {
            ObservableTableModel<CustomerRow> model = ObservableTableModel.of(
                    ObservableArrayList.of(List.of(new CustomerRow("Acme", 3))),
                    customerTable()
            );
            JTable table = new JTable(model);
            table.createDefaultColumnsFromModel();
            configureTable(table);
            table.setToolTipText("original");
            TableCellTooltipBinding<CustomerRow> binding = TableCellTooltipBinding.install(table, model, Resources.empty());

            mouseMoved(table, 5, 5);
            assertThat(table.getToolTipText()).isNull();

            binding.close();
            assertThat(table.getToolTipText()).isEqualTo("original");
            assertThat(table.getMouseMotionListeners())
                    .noneMatch(TableCellTooltipBindingTest::isBindingListener);
            assertThat(table.getMouseListeners())
                    .noneMatch(TableCellTooltipBindingTest::isBindingListener);
            model.close();
        });
    }

    @Test
    void usesModelIndexAfterColumnsAreReordered() {
        SwingEdt.runAndWait(() -> {
            Fixture fixture = fixture(Resources.of(Map.of(
                    NAME_TOOLTIP.id(), "Customer display name",
                    ORDERS_TOOLTIP.id(), "Number of open orders"
            )));
            fixture.table().getColumnModel().moveColumn(0, 1);

            mouseMoved(fixture.table(), 5, 5);

            assertThat(fixture.table().getToolTipText()).isEqualTo("Number of open orders");
            fixture.close();
        });
    }

    @Test
    void clearsTooltipOutsideLiveCells() {
        SwingEdt.runAndWait(() -> {
            Fixture fixture = fixture(Resources.of(Map.of(
                    NAME_TOOLTIP.id(), "Customer display name"
            )));

            mouseMoved(fixture.table(), 5, 5);
            assertThat(fixture.table().getToolTipText()).isEqualTo("Customer display name");

            mouseMoved(fixture.table(), 5, 45);
            assertThat(fixture.table().getToolTipText()).isNull();
            fixture.close();
        });
    }

    private static void mouseMoved(JTable table, int x, int y) {
        MouseEvent event = new MouseEvent(
                table,
                MouseEvent.MOUSE_MOVED,
                0L,
                0,
                x,
                y,
                0,
                false
        );
        for (var listener : table.getMouseMotionListeners()) {
            listener.mouseMoved(event);
        }
    }

    private static boolean isBindingListener(Object listener) {
        return listener.getClass().getName().contains(TableCellTooltipBinding.class.getSimpleName());
    }

    private static Fixture fixture(Resources resources) {
        ObservableTableModel<CustomerRow> model = ObservableTableModel.of(
                ObservableArrayList.of(List.of(new CustomerRow("Acme", 3))),
                customerTable()
        );
        JTable table = new JTable(model);
        table.createDefaultColumnsFromModel();
        configureTable(table);
        TableCellTooltipBinding<CustomerRow> binding = TableCellTooltipBinding.install(table, model, resources);
        return new Fixture(model, table, binding);
    }

    private static void configureTable(JTable table) {
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setWidth(75);
            table.getColumnModel().getColumn(i).setPreferredWidth(75);
        }
        table.setRowHeight(20);
        table.setSize(150, 20);
    }

    private static TableDescriptor<CustomerRow> customerTable() {
        return new TableDescriptor<>(
                TableKey.of("customers", CustomerRow.class),
                List.of(
                        column("name", String.class, NAME_HEADER, NAME_TOOLTIP),
                        column("orders", Integer.class, ORDERS_HEADER, ORDERS_TOOLTIP)
                )
        );
    }

    private static <V> Column<CustomerRow, V> column(
            String id,
            Class<V> valueType,
            ResourceKey<String> headerKey,
            ResourceKey<String> tooltipKey
    ) {
        return Column.readOnly(
                new ColumnDescriptor<>(
                        ColumnKey.of(id, CustomerRow.class, valueType),
                        headerKey,
                        tooltipKey,
                        ColumnDefaults.visible(75, 0),
                        ColumnCapabilities.readOnly()
                ),
                row -> valueType.cast("orders".equals(id) ? row.orders() : row.name())
        );
    }

    private record CustomerRow(String name, int orders) {
    }

    private record Fixture(
            ObservableTableModel<CustomerRow> model,
            JTable table,
            TableCellTooltipBinding<CustomerRow> binding
    ) {

        void close() {
            binding.close();
            model.close();
        }
    }
}
