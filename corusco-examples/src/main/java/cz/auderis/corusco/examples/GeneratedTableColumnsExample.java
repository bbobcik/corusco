package cz.auderis.corusco.examples;

import ca.odell.glazedlists.BasicEventList;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.glazedlists.GlazedListsAdapters;
import cz.auderis.corusco.glazedlists.GlazedObservableList;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.core.table.InMemoryTableStateStore;
import cz.auderis.corusco.core.table.TableStateStore;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import cz.auderis.corusco.swing.table.TableStateController;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;

/**
 * Demonstrates generated table column metadata from annotations.
 */
public final class GeneratedTableColumnsExample {

    private GeneratedTableColumnsExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Reads generated table metadata and creates a Swing table model.
     *
     * @return table diagnostics
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            BasicEventList<GeneratedCustomerRow> eventList = new BasicEventList<>(new ArrayList<>(List.of(
                    new GeneratedCustomerRow("Acme", 2),
                    new GeneratedCustomerRow("Globex", 5)
            )));
            GlazedObservableList<GeneratedCustomerRow> rows = GlazedListsAdapters.observableList(eventList);
            TableStateStore tableStateStore = new InMemoryTableStateStore();

            // Generated binding helpers install the descriptor-backed model and
            // put model cleanup under the same lifecycle as other Swing bindings.
            JTable table = new JTable();
            try (BindingScope scope = new BindingScope()) {
                ObservableTableModel<GeneratedCustomerRow> model =
                        GeneratedCustomerRowTableBindings.installModel(table, rows, scope);
                TableStateController<GeneratedCustomerRow> stateController = scope.add(
                        TableStateController.install(table, model, tableStateStore)
                );

                result.add(GeneratedCustomerRowColumns.NAME_KEY.id());
                result.add(model.getColumnName(0));

                // Table resource keys live in a generated companion class,
                // while help topics travel with the descriptor for later help
                // behaviors.
                result.add(GeneratedCustomerRowTableResources.NAME_TOOLTIP.id());
                result.add(GeneratedCustomerRowColumns.NAME_DESCRIPTOR.helpTopic().id());

                // Persistence metadata is still declarative here. The next
                // table state stage can use it to map stored state and clamp
                // widths.
                result.add(GeneratedCustomerRowColumns.NAME_DESCRIPTOR.persistence().id());
                result.add(Integer.toString(GeneratedCustomerRowColumns.NAME_DESCRIPTOR.persistence().maxWidth()));

                // The state controller bridges JTable's mutable TableColumn
                // model back to generated persistence ids before saving.
                table.getColumnModel().moveColumn(1, 0);
                stateController.saveNow();
                result.add(tableStateStore.load(GeneratedCustomerRowColumns.TABLE.id())
                        .orElseThrow()
                        .columns()
                        .getFirst()
                        .id());

                // Primitive record components are exposed through boxed column
                // key classes, while row values still come from direct accessor
                // calls.
                result.add(model.getColumnClass(1).getSimpleName());
                result.add(model.getValueAt(1, 1).toString());

                // Editing a generated column calls a generated updater helper
                // that creates a replacement record with the edited component
                // value.
                result.add(Boolean.toString(model.isCellEditable(0, 0)));
                model.setValueAt("Acme Corp", 0, 0);
                result.add(eventList.get(0).name());

                // Selection helpers delegate to the runtime binding so sorted
                // JTable view rows still map back to stable model-row values.
                SimpleValue<Integer> selectedModelRow = SimpleValue.empty();
                SimpleValue<GeneratedCustomerRow> selectedRow = SimpleValue.empty();
                GeneratedCustomerRowTableBindings.bindSelection(table, model, selectedModelRow, selectedRow, scope);
                table.getSelectionModel().setSelectionInterval(1, 1);
                result.add(selectedModelRow.value() + ":" + selectedRow.value().name());

                // A Glazed Lists EventList is a first-class row source because
                // the adapter implements ObservableList; generated descriptors
                // do not need a separate table-model path for mature EventList
                // pipelines.
                eventList.add(new GeneratedCustomerRow("Initech", 1));
                result.add(Integer.toString(model.getRowCount()));
            }
            rows.close();
        });
        return List.copyOf(result);
    }
}
