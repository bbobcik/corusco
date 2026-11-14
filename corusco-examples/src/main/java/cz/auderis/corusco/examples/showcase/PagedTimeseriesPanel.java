package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.core.data.CoruscoDataFilter;
import cz.auderis.corusco.core.data.CoruscoDataFilterOperator;
import cz.auderis.corusco.core.data.CoruscoDataPhase;
import cz.auderis.corusco.core.data.CoruscoDataQuery;
import cz.auderis.corusco.core.data.CoruscoDataSort;
import cz.auderis.corusco.core.data.CoruscoRowIdentity;
import cz.auderis.corusco.core.data.DefaultCoruscoDataSource;
import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import cz.auderis.corusco.swing.table.PagedTableBinding;
import cz.auderis.corusco.swing.table.render.EpochUnit;
import cz.auderis.corusco.swing.table.render.OptimizedTableRenderers;
import cz.auderis.corusco.swing.table.render.TimestampRendererOptions;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/** Showcase-only controls; sorting and filtering are applied to the H2 query. */
final class PagedTimeseriesPanel extends JPanel {
    private final JTextField symbol = new JTextField(10);
    private final JTextField region = new JTextField(10);
    private final JComboBox<String> sort = new JComboBox<>(new String[] {"Timestamp", "Symbol"});
    private final JComboBox<CoruscoDataSort.Direction> direction = new JComboBox<>(CoruscoDataSort.Direction.values());
    private final JButton previous = new JButton("Previous"), next = new JButton("Next"), retry = new JButton("Retry");
    private final JLabel status = new JLabel("Select this tab to load data");
    private final DefaultCoruscoDataSource<TimeseriesObservation, Long> source;
    private final PagedTableBinding<TimeseriesObservation, Long> binding;
    private boolean started;

    PagedTimeseriesPanel(BindingScope scope, TaskService tasks, Resources resources) {
        super(new MigLayout("fill, insets 16", "[][grow][][grow][][][][]", "[][][grow][]"));
        source = new DefaultCoruscoDataSource<>(CoruscoRowIdentity.of(TimeseriesObservation.class, Long.class, TimeseriesObservation::sequence), H2TimeseriesRepository::loadPage, tasks);
        JTable table = new JTable();
        binding = new PagedTableBinding<>(table, source, TimeseriesObservationTableDescriptor.DESCRIPTOR, 250, this::refreshStatus);
        scope.add(source::close); scope.add(binding);
        @SuppressWarnings("unchecked")
        ObservableTableModel<TimeseriesObservation> model = (ObservableTableModel<TimeseriesObservation>) table.getModel();
        ShowcaseUi.applyTableResources(table, model, resources, scope);
        scope.add(OptimizedTableRenderers.installTimestampRenderer(table, model, TimeseriesObservationColumns.TIMESTAMP_MILLIS.key(),
                new TimestampRendererOptions(EpochUnit.MILLIS, ZoneId.systemDefault(), "yyyy-MM-dd HH:mm:ss.SSS", true, true, 2048, "")));
        scope.add(ShowcaseUi.installColumnRenderer(table, model, TimeseriesObservationColumns.STATE.key(), ShowcaseVisualRenderer.state(true)));
        scope.add(ShowcaseUi.installColumnRenderer(table, model, TimeseriesObservationColumns.REGION.key(), ShowcaseVisualRenderer.region(true)));
        JButton apply = new JButton("Apply");
        add(new JLabel("Symbol contains")); add(symbol, "growx"); add(new JLabel("Region contains")); add(region, "growx");
        add(new JLabel("Sort")); add(sort); add(direction); add(apply, "wrap");
        add(new JLabel("Case-insensitive literal contains; % and _ are ordinary characters."), "span, wrap");
        add(new JScrollPane(table), "span, grow, wrap");
        JPanel controls = new JPanel(); controls.add(previous); controls.add(next); controls.add(retry); controls.add(status);
        add(controls, "span, growx");
        apply.addActionListener(event -> apply());
        symbol.addActionListener(event -> apply()); region.addActionListener(event -> apply());
        previous.addActionListener(event -> binding.previous()); next.addActionListener(event -> binding.next());
        retry.addActionListener(event -> binding.retry());
        refreshStatus();
    }

    void activate() { if (!started) { started = true; apply(); } }
    private void apply() {
        List<CoruscoDataFilter> filters = new ArrayList<>();
        addFilter(filters, TimeseriesObservationColumns.SYMBOL.key().id(), symbol.getText());
        addFilter(filters, TimeseriesObservationColumns.REGION.key().id(), region.getText());
        binding.query(new CoruscoDataQuery(filters, List.of(new CoruscoDataSort(
                sort.getSelectedIndex() == 0 ? TimeseriesObservationColumns.TIMESTAMP_MILLIS.key().id() : TimeseriesObservationColumns.SYMBOL.key().id(),
                (CoruscoDataSort.Direction) direction.getSelectedItem(), 0))));
    }
    private static void addFilter(List<CoruscoDataFilter> filters, String field, String text) {
        if (!text.isBlank()) filters.add(new CoruscoDataFilter(field, CoruscoDataFilterOperator.CONTAINS, List.of(text.trim())));
    }
    private void refreshStatus() {
        if (binding == null) return;
        previous.setEnabled(binding.canPrevious()); next.setEnabled(binding.canNext());
        var phase = source.status().value().phase(); retry.setEnabled(phase == CoruscoDataPhase.FAILED);
        var shown = binding.displayed();
        String error = phase == CoruscoDataPhase.FAILED && source.status().value().error() != null
                ? ": " + source.status().value().error().message() : "";
        status.setText(phase + error + " — requested " + binding.requested().offset() +
                (shown == null ? "; no page displayed" : "; displayed " + shown.range().offset() + " (" + source.rows().size() + " rows)"));
    }
}
