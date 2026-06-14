package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.core.command.MutableCommand;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.core.table.InMemoryTableStateStore;
import cz.auderis.corusco.core.table.TableStateStore;
import cz.auderis.corusco.core.task.TaskCallbacks;
import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.command.SwingActionAdapter;
import cz.auderis.corusco.swing.task.BusyOverlayBinding;
import cz.auderis.corusco.swing.task.SwingTaskServices;
import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;

final class ShowcaseRuntime implements AutoCloseable {

    private final BindingScope scope = new BindingScope();
    private final TaskService taskService = SwingTaskServices.virtualThreads();
    private final SimpleValue<Boolean> busy = SimpleValue.of(false);
    private final JLabel statusLabel = new JLabel("Ready");
    private final TimeseriesDatabasePanel timeseriesPanel;
    private final EventStreamPanel eventPanel;
    private final CustomerWorkspacePanel customerPanel;
    private final GeneratedTableShowcasePanel generatedPanel;
    private final ShowcaseOverviewPanel overviewPanel;
    private final List<SwingActionAdapter> actions;
    private final JLayer<JPanel> layer;
    private final JMenuBar menuBar;

    private final MutableCommand saveCommand;
    private final MutableCommand addEventCommand;
    private final MutableCommand optimizedCommand;

    ShowcaseRuntime() {
        SwingEdt.requireEdt();
        Resources resources = ShowcaseResources.create();
        TableStateStore stateStore = new InMemoryTableStateStore();
        customerPanel = new CustomerWorkspacePanel(scope, resources, statusLabel);
        generatedPanel = new GeneratedTableShowcasePanel(scope, stateStore, resources, statusLabel);
        timeseriesPanel = new TimeseriesDatabasePanel(scope, stateStore, resources, statusLabel);
        eventPanel = new EventStreamPanel(scope, resources, statusLabel);
        statusLabel.setText("Loading 100K H2 observations...");
        timeseriesPanel.loadRows(H2TimeseriesRepository.loadObservations());
        statusLabel.setText("Loaded " + timeseriesPanel.model.getRowCount() + " observations from H2.");
        overviewPanel = new ShowcaseOverviewPanel();
        overviewPanel.record("Loaded 100K H2 observations");

        ShowcasePresenter presenter = new ShowcasePresenter();
        saveCommand = ShowcasePresenterActions.saveCommand(presenter);
        MutableCommand resetCommand = ShowcasePresenterActions.resetCommand(presenter);
        MutableCommand reloadCommand = ShowcasePresenterActions.reloadCommand(presenter);
        addEventCommand = ShowcasePresenterActions.addEventCommand(presenter);
        optimizedCommand = ShowcasePresenterActions.toggleOptimizedRenderersCommand(presenter);
        MutableCommand docsCommand = ShowcasePresenterActions.openDocsCommand(presenter);
        optimizedCommand.setSelected(true);
        presenter.configure(
                this::saveCustomer,
                () -> {
                    customerPanel.model.reset();
                    customerPanel.refreshFormState();
                    eventPanel.addSyntheticEvent("Customer form reset");
                    recordActivity("Customer form reset to baseline");
                    statusLabel.setText("Customer form reset to baseline.");
                    refreshOverview();
                },
                this::reloadTimeseries,
                () -> {
                    eventPanel.addSyntheticEvent("Manual operations event");
                    recordActivity("Added operations event");
                    statusLabel.setText("Added event row through observable list.");
                    refreshOverview();
                },
                () -> {
                    boolean selected = optimizedCommand.isSelected();
                    timeseriesPanel.setOptimizedRenderers(selected);
                    eventPanel.setOptimizedRenderers(selected);
                    eventPanel.addSyntheticEvent("Renderer mode: " + (selected ? "cached" : "formatter"));
                    recordActivity("Renderer mode changed to " + (selected ? "cached" : "formatter"));
                    statusLabel.setText("Optimized renderers " + (selected ? "enabled." : "disabled."));
                    refreshOverview();
                },
                this::openDocs
        );

        actions = List.of(
                new SwingActionAdapter(saveCommand, key -> ShowcaseResources.resolve(resources, key)),
                new SwingActionAdapter(resetCommand, key -> ShowcaseResources.resolve(resources, key)),
                new SwingActionAdapter(reloadCommand, key -> ShowcaseResources.resolve(resources, key)),
                new SwingActionAdapter(addEventCommand, key -> ShowcaseResources.resolve(resources, key)),
                new SwingActionAdapter(optimizedCommand, key -> ShowcaseResources.resolve(resources, key)),
                new SwingActionAdapter(docsCommand, key -> ShowcaseResources.resolve(resources, key))
        );
        actions.forEach(scope::add);

        JPanel shell = new JPanel(new MigLayout("fill, insets 0", "[260!][grow]", "[][grow][]"));
        shell.add(ShowcaseUi.sidebar(), "cell 0 0 1 3, grow");
        shell.add(toolbar(), "cell 1 0, growx");
        shell.add(tabs(), "cell 1 1, grow");
        shell.add(statusBar(), "cell 1 2, growx");
        layer = new JLayer<>(shell);
        scope.add(BusyOverlayBinding.install(layer, busy));
        menuBar = buildMenuBar();
        refreshOverview();
    }

    JLayer<JPanel> layer() {
        return layer;
    }

    JMenuBar menuBar() {
        return menuBar;
    }

    List<String> diagnostics() {
        timeseriesPanel.symbolFilter.setText("ALFA");
        timeseriesPanel.regionFilter.setText("North");
        timeseriesPanel.applyFilters();
        customerPanel.nameField.setText("Alicia");
        saveCommand.execute();
        addEventCommand.execute();
        return List.of(
                "generatedRows=" + generatedPanel.model.getRowCount(),
                "timeseriesRows=" + timeseriesPanel.model.getRowCount(),
                "timeseriesColumns=" + timeseriesPanel.model.getColumnCount(),
                "visibleTimeseriesRows=" + timeseriesPanel.table.getRowCount(),
                "timeseriesHeader=" + timeseriesPanel.table.getColumnModel()
                        .getColumn(timeseriesPanel.table.convertColumnIndexToView(1))
                        .getHeaderValue(),
                "timeseriesRowHeight=" + timeseriesPanel.rowHeight(),
                "timeseriesStateRenderer=" + timeseriesPanel.stateRendererName(),
                "timeseriesRegionRenderer=" + timeseriesPanel.regionRendererName(),
                "timeseriesTimestamp=" + timeseriesPanel.firstTimestampText(),
                "eventTimestamp=" + eventPanel.firstTimestampText(),
                "events=" + eventPanel.table.getRowCount(),
                "saved=" + customerPanel.model.toResult().name(),
                "status=" + statusLabel.getText()
        );
    }

    @Override
    public void close() {
        SwingEdt.requireEdt();
        scope.close();
        taskService.close();
    }

    private void saveCustomer() {
        if (!customerPanel.model.isCommittable()) {
            statusLabel.setText("Customer form has validation errors.");
            return;
        }
        ShowcaseCustomerEdit saved = customerPanel.model.toResult();
        customerPanel.model.acceptCurrentValues();
        customerPanel.refreshFormState();
        eventPanel.addSyntheticEvent("Saved customer " + saved.name());
        recordActivity("Saved customer " + saved.name());
        statusLabel.setText("Saved " + saved.name());
        refreshOverview();
    }

    private void reloadTimeseries() {
        busy.setValue(true, ChangeOrigin.USER);
        statusLabel.setText("Loading 100K H2 observations...");
        taskService.submit(
                cancellation -> H2TimeseriesRepository.loadObservations(),
                new TaskCallbacks<>() {
                    @Override
                    public void succeeded(List<TimeseriesObservation> rows) {
                        timeseriesPanel.loadRows(rows);
                        busy.setValue(false, ChangeOrigin.SYSTEM);
                        eventPanel.addSyntheticEvent("Reloaded H2 time-series observations");
                        recordActivity("Reloaded " + rows.size() + " H2 observations");
                        statusLabel.setText("Loaded " + rows.size() + " observations from H2.");
                        refreshOverview();
                    }

                    @Override
                    public void failed(Throwable error) {
                        busy.setValue(false, ChangeOrigin.SYSTEM);
                        recordActivity("H2 load failed");
                        statusLabel.setText("H2 load failed: " + error.getMessage());
                        refreshOverview();
                    }
                }
        );
    }

    private void openDocs() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create("https://github.com/auderis/corusco"));
                recordActivity("Opened project documentation");
                statusLabel.setText("Opened project documentation.");
            } else {
                statusLabel.setText("Desktop browsing is not available.");
            }
        } catch (Exception e) {
            statusLabel.setText("Could not open documentation: " + e.getMessage());
        }
    }

    private JToolBar toolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(8, 8, 8, 8));
        for (int i = 0; i < actions.size(); i++) {
            if (i == 2 || i == 4) {
                toolbar.addSeparator();
            }
            AbstractButton button = (i == 4) ? new JToggleButton(actions.get(i)) : new JButton(actions.get(i));
            button.setFocusable(false);
            toolbar.add(button);
        }
        return toolbar;
    }

    private JTabbedPane tabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", overviewPanel);
        tabs.addTab("Customer Workflow", customerPanel);
        tabs.addTab("Market Data Grid", timeseriesPanel);
        tabs.addTab("Generated Table Lab", generatedPanel);
        tabs.addTab("Event Stream", eventPanel);
        return tabs;
    }

    private JPanel statusBar() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 8 12", "[grow][]", "[]"));
        panel.add(statusLabel, "growx");
        panel.add(new JLabel("EDT-bound Swing adapters active"));
        return panel;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.add(new JMenuItem(actions.get(0)));
        file.add(new JMenuItem(actions.get(1)));
        file.add(new JSeparator());
        file.add(new JMenuItem(actions.get(5)));
        JMenu data = new JMenu("Data");
        data.add(new JMenuItem(actions.get(2)));
        data.add(new JMenuItem(actions.get(3)));
        JMenu view = new JMenu("View");
        view.add(new JCheckBoxMenuItem(actions.get(4)));
        bar.add(file);
        bar.add(data);
        bar.add(view);
        return bar;
    }

    private void refreshOverview() {
        overviewPanel.refresh(
                timeseriesPanel.model.getRowCount(),
                timeseriesPanel.table.getRowCount(),
                generatedPanel.model.getRowCount(),
                optimizedCommand.isSelected(),
                eventPanel.eventCount(),
                customerPanel.model.toResult().name()
        );
    }

    private void recordActivity(String message) {
        overviewPanel.record(message);
    }
}
