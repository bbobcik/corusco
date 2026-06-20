package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.BindingFactory;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import cz.auderis.corusco.swing.table.render.EpochUnit;
import cz.auderis.corusco.swing.table.render.OptimizedTableRenderers;
import cz.auderis.corusco.swing.table.render.TimestampRendererOptions;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import net.miginfocom.swing.MigLayout;

final class EventStreamPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final ObservableArrayList<AuditEvent> rows = ObservableArrayList.of(List.of(
            new AuditEvent(1_766_000_000_000L, ObservationState.OPEN, "Form validation evaluated"),
            new AuditEvent(1_766_000_060_000L, ObservationState.RUNNING, "Database table refreshed"),
            new AuditEvent(1_766_000_120_000L, ObservationState.CLOSED, "Table state saved")
    ));
    private final ObservableTableModel<AuditEvent> model =
            ObservableTableModel.of(rows, AuditEventTableDescriptor.DESCRIPTOR);
    final JTable table = new JTable(model);
    private final JLabel eventCountValue = new JLabel(Integer.toString(rows.size()));
    private final JLabel rendererValue = new JLabel("Cached");
    private final List<Binding> rendererBindings = new ArrayList<>();

    EventStreamPanel(BindingScope scope, Resources resources, JLabel statusLabel) {
        super(new MigLayout("fill, insets 20, gap 12", "[grow][180!][180!]", "[][][grow][]"));
        scope.add(model);
        table.setAutoCreateRowSorter(true);
        ShowcaseUi.applyTableResources(table, model, resources, scope);
        scope.add(BindingFactory.statusText(table, statusLabel, "Event stream uses optimized timestamp and state renderers."));
        scope.add(() -> rendererBindings.forEach(Binding::close));
        add(header(), "growx");
        add(ShowcaseUi.metricValue("Events", eventCountValue, "Observable row source"), "growx");
        add(ShowcaseUi.metricValue("Renderer", rendererValue, "Timestamp plus state"), "growx, wrap");
        add(new JScrollPane(table), "span, grow, wrap");
        add(ShowcaseUi.caption("The stream receives command activity, validation results and data-load events."),
                "span, growx");
        setOptimizedRenderers(true);
    }

    void addSyntheticEvent(String message) {
        rows.add(new AuditEvent(
                1_766_000_000_000L + rows.size() * 60_000L,
                ObservationState.RUNNING,
                message
        ));
        eventCountValue.setText(Integer.toString(rows.size()));
    }

    int eventCount() {
        return rows.size();
    }

    void setOptimizedRenderers(boolean enabled) {
        rendererBindings.forEach(Binding::close);
        rendererBindings.clear();
        rendererBindings.add(OptimizedTableRenderers.installTimestampRenderer(
                table,
                model,
                AuditEventColumns.TIMESTAMP_MILLIS_KEY,
                new TimestampRendererOptions(EpochUnit.MILLIS, ZoneId.systemDefault(),
                        "yyyy-MM-dd HH:mm:ss.SSS", true, enabled, 256, "")
        ));
        rendererBindings.add(ShowcaseUi.installColumnRenderer(
                table,
                model,
                AuditEventColumns.STATE_KEY,
                ShowcaseVisualRenderer.state(enabled)
        ));
        rendererValue.setText(enabled ? "Cached visuals" : "Live visuals");
        table.repaint();
    }

    String firstTimestampText() {
        int viewColumn = table.convertColumnIndexToView(0);
        if (table.getRowCount() == 0 || viewColumn < 0) {
            return "";
        }
        TableCellRenderer renderer = table.getCellRenderer(0, viewColumn);
        java.awt.Component component = renderer.getTableCellRendererComponent(
                table,
                table.getValueAt(0, viewColumn),
                false,
                false,
                0,
                viewColumn
        );
        return component instanceof JLabel label ? label.getText() : "";
    }

    private JPanel header() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 0", "[grow][]", "[][]"));
        panel.add(ShowcaseUi.heading("Operations Event Stream"), "growx");
        panel.add(ShowcaseUi.badge("Live feed"), "wrap");
        panel.add(ShowcaseUi.caption("Command and data events rendered through the same descriptor-backed table path."),
                "span, growx");
        return panel;
    }
}
