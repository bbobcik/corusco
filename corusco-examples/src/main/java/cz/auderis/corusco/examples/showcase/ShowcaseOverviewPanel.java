package cz.auderis.corusco.examples.showcase;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;

final class ShowcaseOverviewPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);

    private final JLabel rowsValue = new JLabel("0");
    private final JLabel visibleRowsValue = new JLabel("0");
    private final JLabel rendererValue = new JLabel("Cached");
    private final JLabel customerValue = new JLabel("-");
    private final JLabel generatedRowsValue = new JLabel("0");
    private final JLabel eventsValue = new JLabel("0");
    private final DefaultListModel<String> activityModel = new DefaultListModel<>();

    ShowcaseOverviewPanel() {
        super(new MigLayout("fill, insets 20, gap 14", "[grow][grow][grow]", "[][grow][][grow]"));
        add(header(), "span, growx, wrap");
        add(ShowcaseUi.metricValue("H2 observations", rowsValue, "Tidy market data"), "growx");
        add(ShowcaseUi.metricValue("Visible after filter", visibleRowsValue, "JTable row sorter"), "growx");
        add(ShowcaseUi.metricValue("Renderer mode", rendererValue, "Timestamp and state cells"), "growx, wrap");
        add(ShowcaseUi.metricValue("Generated form result", customerValue, "Committed customer"), "growx");
        add(ShowcaseUi.metricValue("Generated table rows", generatedRowsValue, "Glazed Lists bridge"), "growx");
        add(ShowcaseUi.metricValue("Event stream", eventsValue, "Observable feed"), "growx, wrap");
        add(activityPanel(), "span, grow");
        record("Showcase initialized");
    }

    void refresh(
            int totalRows,
            int visibleRows,
            int generatedRows,
            boolean optimizedRenderers,
            int eventCount,
            String customerName
    ) {
        rowsValue.setText(String.format(Locale.US, "%,d", totalRows));
        visibleRowsValue.setText(String.format(Locale.US, "%,d", visibleRows));
        generatedRowsValue.setText(Integer.toString(generatedRows));
        rendererValue.setText(optimizedRenderers ? "Cached" : "Formatter");
        eventsValue.setText(Integer.toString(eventCount));
        customerValue.setText(customerName);
    }

    void record(String message) {
        activityModel.add(0, TIME_FORMAT.format(LocalTime.now()) + "  " + message);
        while (activityModel.size() > 8) {
            activityModel.removeElementAt(activityModel.size() - 1);
        }
    }

    private JPanel header() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 0", "[grow][]", "[][]"));
        panel.add(ShowcaseUi.heading("Corusco Operations Console"), "growx");
        panel.add(ShowcaseUi.badge("Live showcase"), "wrap");
        panel.add(ShowcaseUi.caption("Generated metadata, Swing bindings and high-volume tables composed into one desktop workflow."),
                "span, growx");
        return panel;
    }

    private JPanel activityPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 16, gap 10", "[grow]", "[][grow]"));
        ShowcaseUi.card(panel);
        panel.add(ShowcaseUi.heading("Recent Activity"), "growx, wrap");
        JList<String> activity = new JList<>(activityModel);
        activity.setVisibleRowCount(8);
        panel.add(new JScrollPane(activity), "grow");
        return panel;
    }
}
