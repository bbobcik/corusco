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
import javax.swing.table.JTableHeader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableHeaderTooltipBindingTest {

    private static final ResourceKey<String> NAME_HEADER = ResourceKey.of("customers/name/header", String.class);
    private static final ResourceKey<String> NAME_TOOLTIP = ResourceKey.of("customers/name/tooltip", String.class);
    private static final ResourceKey<String> ORDERS_HEADER = ResourceKey.of("customers/orders/header", String.class);
    private static final ResourceKey<String> ORDERS_TOOLTIP = ResourceKey.of("customers/orders/tooltip", String.class);

    @Test
    void showsTooltipForHoveredHeaderColumn() {
        SwingEdt.runAndWait(() -> {
            Fixture fixture = fixture(Resources.of(Map.of(
                    NAME_TOOLTIP.id(), "Customer display name",
                    ORDERS_TOOLTIP.id(), "Number of open orders"
            )));

            mouseMoved(fixture.header(), 5);
            assertThat(fixture.header().getToolTipText()).isEqualTo("Customer display name");

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
            configureColumns(table);
            JTableHeader header = table.getTableHeader();
            header.setToolTipText("original");
            int motionListenerCount = header.getMouseMotionListeners().length;
            TableHeaderTooltipBinding<CustomerRow> binding = TableHeaderTooltipBinding.install(table, model, Resources.empty());

            mouseMoved(header, 5);
            assertThat(header.getToolTipText()).isNull();

            binding.close();
            assertThat(header.getToolTipText()).isEqualTo("original");
            assertThat(header.getMouseMotionListeners()).hasSize(motionListenerCount);
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

            mouseMoved(fixture.header(), 5);

            assertThat(fixture.header().getToolTipText()).isEqualTo("Number of open orders");
            fixture.close();
        });
    }

    private static void mouseMoved(JTableHeader header, int x) {
        MouseEvent event = new MouseEvent(
                header,
                MouseEvent.MOUSE_MOVED,
                0L,
                0,
                x,
                5,
                0,
                false
        );
        header.getMouseMotionListeners()[header.getMouseMotionListeners().length - 1].mouseMoved(event);
    }

    private static Fixture fixture(Resources resources) {
        ObservableTableModel<CustomerRow> model = ObservableTableModel.of(
                ObservableArrayList.of(List.of(new CustomerRow("Acme", 3))),
                customerTable()
        );
        JTable table = new JTable(model);
        table.createDefaultColumnsFromModel();
        configureColumns(table);
        TableHeaderTooltipBinding<CustomerRow> binding = TableHeaderTooltipBinding.install(table, model, resources);
        return new Fixture(model, table, binding);
    }

    private static void configureColumns(JTable table) {
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setWidth(75);
            table.getColumnModel().getColumn(i).setPreferredWidth(75);
        }
        table.setSize(150, 20);
        table.getTableHeader().setSize(150, 20);
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
            TableHeaderTooltipBinding<CustomerRow> binding
    ) {

        JTableHeader header() {
            return table.getTableHeader();
        }

        void close() {
            binding.close();
            model.close();
        }
    }
}
