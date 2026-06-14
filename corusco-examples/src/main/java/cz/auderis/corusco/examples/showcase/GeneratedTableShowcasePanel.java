package cz.auderis.corusco.examples.showcase;

import ca.odell.glazedlists.BasicEventList;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.core.table.TableStateStore;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.examples.generated.GeneratedCustomerRow;
import cz.auderis.corusco.examples.generated.GeneratedCustomerRowTableBindings;
import cz.auderis.corusco.glazedlists.GlazedListsAdapters;
import cz.auderis.corusco.glazedlists.GlazedObservableList;
import cz.auderis.corusco.swing.binding.BindingFactory;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import cz.auderis.corusco.swing.table.TableHeaderColumnVisibilityMenu;
import cz.auderis.corusco.swing.table.TableSelectionBinding;
import cz.auderis.corusco.swing.table.TableStateController;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import net.miginfocom.swing.MigLayout;

final class GeneratedTableShowcasePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    final ObservableTableModel<GeneratedCustomerRow> model;
    private final JLabel rowCountValue = new JLabel("0");

    GeneratedTableShowcasePanel(BindingScope scope, TableStateStore stateStore, Resources resources, JLabel statusLabel) {
        super(new MigLayout("fill, insets 20, gap 12", "[grow][180!]", "[][grow][]"));
        BasicEventList<GeneratedCustomerRow> eventList = new BasicEventList<>(new ArrayList<>(List.of(
                new GeneratedCustomerRow("Alice Retail", 3),
                new GeneratedCustomerRow("Northwind Supply", 7),
                new GeneratedCustomerRow("Globex Wholesale", 5)
        )));
        GlazedObservableList<GeneratedCustomerRow> rows = GlazedListsAdapters.observableList(eventList);
        scope.add(rows::close);
        JTable table = new JTable();
        model = GeneratedCustomerRowTableBindings.installModel(table, rows, scope);
        TableStateController<GeneratedCustomerRow> stateController = scope.add(
                TableStateController.install(table, model, stateStore)
        );
        TableHeaderColumnVisibilityMenu<GeneratedCustomerRow> menu = scope.add(
                TableHeaderColumnVisibilityMenu.install(table, model, stateController)
        );
        rowCountValue.setText(Integer.toString(model.getRowCount()));
        ShowcaseUi.applyTableResources(table, model, resources, scope);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scope.add(TableSelectionBinding.bind(table, model, SimpleValue.empty()));
        scope.add(BindingFactory.statusText(table, statusLabel, "Generated table: descriptors, row updaters, state and selection."));

        add(header(), "growx");
        JButton visibility = new JButton("Column visibility menu");
        visibility.addActionListener(event -> menu.createMenu().show(visibility, 0, visibility.getHeight()));
        add(ShowcaseUi.metricValue("Rows", rowCountValue, "Glazed Lists source"), "growx, wrap");
        add(new JScrollPane(table), "span, grow, wrap");
        add(visibility, "split 2");
        add(ShowcaseUi.caption("Editable values are written back through generated immutable-record updaters."));

        table.getColumnModel().moveColumn(1, 0);
        stateController.saveNow();
    }

    private JPanel header() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 0", "[grow][]", "[][]"));
        panel.add(ShowcaseUi.heading("Generated Table Lab"), "growx");
        panel.add(ShowcaseUi.badge("Stateful columns"), "wrap");
        panel.add(ShowcaseUi.caption("Column keys, headers, help metadata, visibility and row updates come from generated companions."),
                "span, growx");
        return panel;
    }
}
