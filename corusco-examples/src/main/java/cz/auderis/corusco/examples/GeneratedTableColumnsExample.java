package cz.auderis.corusco.examples;

import ca.odell.glazedlists.BasicEventList;
import cz.auderis.corusco.glazedlists.GlazedListsAdapters;
import cz.auderis.corusco.glazedlists.GlazedObservableList;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import java.util.ArrayList;
import java.util.List;

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

            // The generated descriptor keeps table identity, visual defaults,
            // and typed row accessors together without JavaBeans property names.
            ObservableTableModel<GeneratedCustomerRow> model = GeneratedCustomerRowTableDescriptor.tableModel(rows);
            result.add(GeneratedCustomerRowColumns.NAME_KEY.id());
            result.add(model.getColumnName(0));

            // Primitive record components are exposed through boxed column key
            // classes, while row values still come from direct accessor calls.
            result.add(model.getColumnClass(1).getSimpleName());
            result.add(model.getValueAt(1, 1).toString());

            // Editing a generated column calls a generated updater helper that
            // creates a replacement record with the edited component value.
            result.add(Boolean.toString(model.isCellEditable(0, 0)));
            model.setValueAt("Acme Corp", 0, 0);
            result.add(eventList.get(0).name());

            // A Glazed Lists EventList is a first-class row source because the
            // adapter implements ObservableList; generated descriptors do not
            // need a separate table-model path for mature EventList pipelines.
            eventList.add(new GeneratedCustomerRow("Initech", 1));
            result.add(Integer.toString(model.getRowCount()));
            model.close();
            rows.close();
        });
        return List.copyOf(result);
    }
}
