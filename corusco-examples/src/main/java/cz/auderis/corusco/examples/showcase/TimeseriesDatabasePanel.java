package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.core.table.TableStateStore;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.BindingFactory;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import cz.auderis.corusco.swing.table.TableStateController;
import cz.auderis.corusco.swing.table.render.EpochUnit;
import cz.auderis.corusco.swing.table.render.OptimizedTableRenderers;
import cz.auderis.corusco.swing.table.render.TimestampRendererOptions;
import java.awt.Component;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableCellRenderer;
import net.miginfocom.swing.MigLayout;

final class TimeseriesDatabasePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT).withZone(ZoneId.systemDefault());

    private final ObservableArrayList<TimeseriesObservation> rows = ObservableArrayList.empty();
    final ObservableTableModel<TimeseriesObservation> model =
            ObservableTableModel.of(rows, TimeseriesObservationTableDescriptor.DESCRIPTOR);
    final JTable table = new JTable(model);
    final JTextField symbolFilter = new JTextField();
    final JTextField regionFilter = new JTextField();
    private final JLabel rowCountValue = new JLabel("0");
    private final JLabel visibleCountValue = new JLabel("0");
    private final JLabel rendererValue = new JLabel("Formatter");
    private final JLabel timestampValue = new JLabel("-");
    private final TableRowSorter<ObservableTableModel<TimeseriesObservation>> sorter = new TableRowSorter<>(model);
    private final List<Binding> rendererBindings = new ArrayList<>();

    TimeseriesDatabasePanel(BindingScope scope, TableStateStore stateStore, Resources resources, JLabel statusLabel) {
        super(new MigLayout("fill, insets 20, gap 12", "[grow][grow][grow][grow]", "[][][][grow][]"));
        scope.add(model);
        scope.add(TableStateController.install(table, model, stateStore));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowSorter(sorter);
        table.setFillsViewportHeight(true);
        ShowcaseUi.applyTableResources(table, model, resources, scope);
        scope.add(() -> rendererBindings.forEach(Binding::close));

        add(header(), "span, growx, wrap");
        add(ShowcaseUi.metricValue("Loaded rows", rowCountValue, "In-memory H2 tidy data"), "growx");
        add(ShowcaseUi.metricValue("Visible rows", visibleCountValue, "Sorter and filters applied"), "growx");
        add(ShowcaseUi.metricValue("Renderer mode", rendererValue, "Timestamp, state and region"), "growx");
        add(ShowcaseUi.metricValue("First timestamp", timestampValue, "Formatted epoch millis"), "growx, wrap");
        add(filters(), "span, growx, wrap");
        add(new JScrollPane(table), "span, grow, wrap");
        add(ShowcaseUi.caption("Sorting, filtering, column movement, state persistence, header tooltips and optimized renderers stay active on the 100K-row model."), "span, growx");
        symbolFilter.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyFilters));
        regionFilter.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyFilters));
        scope.add(BindingFactory.statusText(symbolFilter, statusLabel, "Filtering a 100K-row H2-loaded table by symbol."));
        scope.add(BindingFactory.statusText(regionFilter, statusLabel, "Filtering a 100K-row H2-loaded table by region."));
        setOptimizedRenderers(true);
    }

    void loadRows(List<TimeseriesObservation> loadedRows) {
        rows.batch(list -> {
            list.clear();
            for (TimeseriesObservation row : loadedRows) {
                list.add(row);
            }
        });
        updateMetrics();
    }

    List<TimeseriesObservation> snapshot() {
        return rows.snapshot();
    }

    void applyFilters() {
        List<RowFilter<ObservableTableModel<TimeseriesObservation>, Integer>> filters = new ArrayList<>();
        addRegexFilter(filters, symbolFilter.getText(), 2);
        addRegexFilter(filters, regionFilter.getText(), 4);
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        updateMetrics();
    }

    void setOptimizedRenderers(boolean enabled) {
        rendererBindings.forEach(Binding::close);
        rendererBindings.clear();
        rendererBindings.add(OptimizedTableRenderers.installTimestampRenderer(
                table,
                model,
                TimeseriesObservationColumns.TIMESTAMP_MILLIS_KEY,
                timestampOptions(enabled, 2_048)
        ));
        rendererBindings.add(ShowcaseUi.installColumnRenderer(
                table,
                model,
                TimeseriesObservationColumns.STATE_KEY,
                ShowcaseVisualRenderer.state(enabled)
        ));
        rendererBindings.add(ShowcaseUi.installColumnRenderer(
                table,
                model,
                TimeseriesObservationColumns.REGION_KEY,
                ShowcaseVisualRenderer.region(enabled)
        ));
        rendererValue.setText(enabled ? "Cached visuals" : "Live visuals");
        table.repaint();
    }

    String firstTimestampText() {
        if (table.getRowCount() == 0) {
            return "";
        }
        int viewColumn = table.convertColumnIndexToView(1);
        if (viewColumn < 0) {
            return "";
        }
        TableCellRenderer renderer = table.getCellRenderer(0, viewColumn);
        Component component = renderer.getTableCellRendererComponent(
                table,
                table.getValueAt(0, viewColumn),
                false,
                false,
                0,
                viewColumn
        );
        return component instanceof JLabel label ? label.getText() : "";
    }

    String stateRendererName() {
        return rendererName(6);
    }

    String regionRendererName() {
        return rendererName(4);
    }

    int rowHeight() {
        return table.getRowHeight();
    }

    private JPanel header() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 0", "[grow][]", "[][]"));
        panel.add(ShowcaseUi.heading("Market Data Time Series"), "growx");
        panel.add(ShowcaseUi.badge("100K H2 rows"), "wrap");
        panel.add(ShowcaseUi.caption("Dense operational grid with generated table descriptors and explicit hot-cell renderers."),
                "span, growx");
        return panel;
    }

    private JPanel filters() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 12, gap 10", "[][180!][][180!][grow]", "[]"));
        ShowcaseUi.card(panel);
        panel.add(new JLabel("Symbol"));
        panel.add(symbolFilter, "growx");
        panel.add(new JLabel("Region"));
        panel.add(regionFilter, "growx");
        JLabel hint = ShowcaseUi.caption("Filters update the row sorter immediately.");
        panel.add(hint, "growx");
        return panel;
    }

    private void updateMetrics() {
        rowCountValue.setText(String.format(Locale.US, "%,d", model.getRowCount()));
        visibleCountValue.setText(String.format(Locale.US, "%,d", table.getRowCount()));
        if (model.getRowCount() > 0) {
            timestampValue.setText(TIMESTAMP_FORMAT.format(Instant.ofEpochMilli(rows.get(0).timestampMillis())));
        } else {
            timestampValue.setText("-");
        }
    }

    private static TimestampRendererOptions timestampOptions(boolean bitmapCache, int cacheSize) {
        return new TimestampRendererOptions(
                EpochUnit.MILLIS,
                ZoneId.systemDefault(),
                "yyyy-MM-dd HH:mm:ss.SSS",
                true,
                bitmapCache,
                cacheSize,
                ""
        );
    }

    private String rendererName(int modelColumn) {
        int viewColumn = table.convertColumnIndexToView(modelColumn);
        if (viewColumn < 0) {
            return "";
        }
        return table.getCellRenderer(0, viewColumn).getClass().getSimpleName();
    }

    private static void addRegexFilter(
            List<RowFilter<ObservableTableModel<TimeseriesObservation>, Integer>> filters,
            String text,
            int column
    ) {
        String value = text.trim();
        if (!value.isBlank()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(value), column));
        }
    }
}
