package cz.auderis.corusco.swing.table.render;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnCapabilities;
import cz.auderis.corusco.core.table.ColumnDefaults;
import cz.auderis.corusco.core.table.ColumnDescriptor;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableKey;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.ZoneId;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptimizedTableRenderersTest {

    private static final ColumnKey<EventRow, Long> TIME_KEY =
            ColumnKey.of("event/time", EventRow.class, Long.class);
    private static final ColumnKey<EventRow, EventState> STATE_KEY =
            ColumnKey.of("event/state", EventRow.class, EventState.class);
    private static final ColumnKey<EventRow, Boolean> ACTIVE_KEY =
            ColumnKey.of("event/active", EventRow.class, Boolean.class);

    @Test
    void validatesTimestampOptions() {
        assertThatThrownBy(() -> new TimestampRendererOptions(
                EpochUnit.MILLIS,
                ZoneId.of("UTC"),
                " ",
                true,
                false,
                16,
                ""
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pattern");
        assertThatThrownBy(() -> new TimestampRendererOptions(
                EpochUnit.MILLIS,
                ZoneId.of("UTC"),
                "yyyy-MM-dd",
                true,
                false,
                0,
                ""
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cacheSize");
    }

    @Test
    void formatsEpochTimestampValues() {
        TimestampRendererOptions options = new TimestampRendererOptions(
                EpochUnit.MILLIS,
                ZoneId.of("UTC"),
                "yyyy-MM-dd HH:mm:ss.SSS",
                true,
                false,
                16,
                "(none)"
        );
        TimestampTableCellRenderer renderer = new TimestampTableCellRenderer(options);

        assertThat(renderer.text(0L)).isEqualTo("1970-01-01 00:00:00.000");
        assertThat(renderer.text(1_234L)).isEqualTo("1970-01-01 00:00:01.234");
        assertThat(renderer.text(null)).isEqualTo("(none)");
    }

    @Test
    void formatsEpochMicroAndNanoValues() {
        TimestampRendererOptions micros = new TimestampRendererOptions(
                EpochUnit.MICROS,
                ZoneId.of("UTC"),
                "yyyy-MM-dd HH:mm:ss.SSS",
                false,
                false,
                16,
                ""
        );
        TimestampRendererOptions nanos = new TimestampRendererOptions(
                EpochUnit.NANOS,
                ZoneId.of("UTC"),
                "yyyy-MM-dd HH:mm:ss.SSS",
                false,
                false,
                16,
                ""
        );

        assertThat(new TimestampTableCellRenderer(micros).text(1_234_000L))
                .isEqualTo("1970-01-01 00:00:01.234");
        assertThat(new TimestampTableCellRenderer(nanos).text(1_234_000_000L))
                .isEqualTo("1970-01-01 00:00:01.234");
    }

    @Test
    void validatesStateOptions() {
        assertThatThrownBy(() -> new StateRendererOptions("yes", "no", "", true, true, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cacheSize");
    }

    @Test
    void formatsBooleanAndEnumStateValues() {
        StateTableCellRenderer renderer = new StateTableCellRenderer(
                new StateRendererOptions("yes", "no", "(none)", true, true, 16)
        );

        assertThat(renderer.text(Boolean.TRUE)).isEqualTo("yes");
        assertThat(renderer.text(Boolean.FALSE)).isEqualTo("no");
        assertThat(renderer.text(EventState.OPEN)).isEqualTo("OPEN");
        assertThat(renderer.text(null)).isEqualTo("(none)");
    }

    @Test
    void installsAndRestoresDefaultRenderer() {
        SwingEdt.runAndWait(() -> {
            JTable table = new JTable();
            TableCellRenderer original = table.getDefaultRenderer(Long.class);

            Binding binding = OptimizedTableRenderers.installTimestampRenderer(
                    table,
                    Long.class,
                    TimestampRendererOptions.defaults()
            );

            assertThat(table.getDefaultRenderer(Long.class)).isInstanceOf(TimestampTableCellRenderer.class);
            binding.close();
            binding.close();
            assertThat(table.getDefaultRenderer(Long.class)).isSameAs(original);
        });
    }

    @Test
    void installsColumnRendererByColumnKeyAfterViewReorder() {
        SwingEdt.runAndWait(() -> {
            ObservableArrayList<EventRow> rows = ObservableArrayList.of(List.of(
                    new EventRow(0L, EventState.OPEN, true)
            ));
            ObservableTableModel<EventRow> model = ObservableTableModel.of(rows, eventTable());
            JTable table = new JTable(model);
            table.moveColumn(0, 2);
            int stateViewColumn = table.convertColumnIndexToView(1);
            TableCellRenderer original = table.getColumnModel().getColumn(stateViewColumn).getCellRenderer();

            Binding binding = OptimizedTableRenderers.installStateRenderer(
                    table,
                    model,
                    STATE_KEY,
                    StateRendererOptions.defaults()
            );

            assertThat(table.getColumnModel().getColumn(stateViewColumn).getCellRenderer())
                    .isInstanceOf(StateTableCellRenderer.class);
            binding.close();
            assertThat(table.getColumnModel().getColumn(stateViewColumn).getCellRenderer()).isSameAs(original);
            model.close();
        });
    }

    @Test
    void paintsTimestampBitmapCacheAndInvalidatesOnVisualChanges() {
        SwingEdt.runAndWait(() -> {
            TimestampTableCellRenderer renderer = new TimestampTableCellRenderer(
                    new TimestampRendererOptions(
                            EpochUnit.MILLIS,
                            ZoneId.of("UTC"),
                            "yyyy-MM-dd HH:mm:ss.SSS",
                            true,
                            true,
                            4,
                            ""
                    )
            );
            JTable table = new JTable(1, 1);

            paint(renderer, table, 1_234L);
            int initialCacheSize = renderer.cachedImageCount();
            paint(renderer, table, 1_999L);
            assertThat(renderer.cachedImageCount()).isGreaterThanOrEqualTo(initialCacheSize);

            table.setFont(table.getFont().deriveFont(Font.BOLD));
            paint(renderer, table, 2_000L);
            table.setForeground(Color.BLUE);
            paint(renderer, table, 3_000L);

            assertThat(renderer.cachedImageCount()).isLessThanOrEqualTo(4);
            assertThat(renderer.cachedImageCount()).isPositive();
        });
    }

    @Test
    void paintsStateBitmapCacheAndBoundsEntries() {
        SwingEdt.runAndWait(() -> {
            StateTableCellRenderer renderer = new StateTableCellRenderer(
                    new StateRendererOptions("enabled", "disabled", "", true, true, 2)
            );
            JTable table = new JTable(1, 1);

            BufferedImage trueImage = paint(renderer, table, true);
            paint(renderer, table, false);
            paint(renderer, table, null);

            assertThat(renderer.cachedImageCount()).isLessThanOrEqualTo(2);
            assertThat(containsPaintedPixels(trueImage)).isTrue();
        });
    }

    @Test
    void benchmarkSmokeComparesRendererPaths() {
        SwingEdt.runAndWait(() -> {
            JTable table = new JTable(1, 1);
            TableCellRenderer defaultRenderer = table.getDefaultRenderer(String.class);
            TimestampRendererOptions formatterOnly = new TimestampRendererOptions(
                    EpochUnit.MILLIS,
                    ZoneId.of("UTC"),
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    true,
                    false,
                    64,
                    ""
            );
            TimestampRendererOptions bitmapPrefix = new TimestampRendererOptions(
                    EpochUnit.MILLIS,
                    ZoneId.of("UTC"),
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    true,
                    true,
                    64,
                    ""
            );
            StateRendererOptions stateOptions = new StateRendererOptions("enabled", "disabled", "", true, true, 16);

            long defaultText = paintMany(defaultRenderer, table, "1970-01-01 00:00:01.234");
            long timestampText = paintMany(new TimestampTableCellRenderer(formatterOnly), table, 1_234L);
            long timestampBitmap = paintMany(new TimestampTableCellRenderer(bitmapPrefix), table, 1_234L);
            long stateBitmap = paintMany(new StateTableCellRenderer(stateOptions), table, EventState.OPEN);

            assertThat(defaultText).isPositive();
            assertThat(timestampText).isPositive();
            assertThat(timestampBitmap).isPositive();
            assertThat(stateBitmap).isPositive();
        });
    }

    private static BufferedImage paint(TableCellRenderer renderer, JTable table, Object value) {
        Component component = renderer.getTableCellRendererComponent(table, value, false, false, 0, 0);
        component.setSize(220, table.getRowHeight());
        BufferedImage image = new BufferedImage(220, table.getRowHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            component.paint(graphics);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static long paintMany(TableCellRenderer renderer, JTable table, Object value) {
        long started = System.nanoTime();
        for (int i = 0; i < 64; i++) {
            paint(renderer, table, value);
        }
        return System.nanoTime() - started;
    }

    private static boolean containsPaintedPixels(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static TableDescriptor<EventRow> eventTable() {
        Column<EventRow, Long> time = Column.readOnly(
                new ColumnDescriptor<>(
                        TIME_KEY,
                        ResourceKey.of("event.time", String.class),
                        null,
                        ColumnDefaults.visible(180, 0),
                        ColumnCapabilities.readOnly()
                ),
                EventRow::timestamp
        );
        Column<EventRow, EventState> state = Column.readOnly(
                new ColumnDescriptor<>(
                        STATE_KEY,
                        ResourceKey.of("event.state", String.class),
                        null,
                        ColumnDefaults.visible(90, 1),
                        ColumnCapabilities.readOnly()
                ),
                EventRow::state
        );
        Column<EventRow, Boolean> active = Column.readOnly(
                new ColumnDescriptor<>(
                        ACTIVE_KEY,
                        ResourceKey.of("event.active", String.class),
                        null,
                        ColumnDefaults.visible(80, 2),
                        ColumnCapabilities.readOnly()
                ),
                EventRow::active
        );
        return new TableDescriptor<>(TableKey.of("events", EventRow.class), List.of(time, state, active));
    }

    private record EventRow(Long timestamp, EventState state, Boolean active) {
    }

    private enum EventState {
        OPEN,
        CLOSED
    }
}
