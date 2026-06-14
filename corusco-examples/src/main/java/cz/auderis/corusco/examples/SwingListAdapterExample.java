package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.collection.EdtObservableList;
import cz.auderis.corusco.swing.collection.ObservableComboBoxModel;
import cz.auderis.corusco.swing.collection.ObservableListModel;
import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * Demonstrates Swing list adapters over one observable source list.
 *
 * <p>The scenario adapts a Corusco observable list for Swing list consumers and
 * verifies that source changes are reflected through the adapter. It helps
 * readers understand where storage ownership stays when Swing models are
 * created.</p>
 */
public final class SwingListAdapterExample {

    private SwingListAdapterExample() {
    }

    /**
     * Runs the example.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        SwingEdt.runAndWait(SwingListAdapterExample::runOnEdt);
    }

    private static void runOnEdt() {
        ObservableArrayList<String> customers = ObservableArrayList.of(java.util.List.of("Acme", "Globex"));
        EdtObservableList<String> swingCustomers = EdtObservableList.of(customers);
        ObservableListModel<String> listModel = ObservableListModel.of(swingCustomers);
        ObservableComboBoxModel<String> comboModel = ObservableComboBoxModel.of(swingCustomers);

        // Swing components consume adapters; application code keeps mutating
        // the observable list so all views see the same ordered data.
        JList<String> list = new JList<>(listModel);
        JComboBox<String> combo = new JComboBox<>(comboModel);
        comboModel.setSelectedItem("Acme");

        // The dispatcher wrapper keeps Swing model notifications on the EDT.
        // Mutations may still target the source list that owns the data.
        customers.batch(items -> {
            items.add("Initech");
            items.set(0, "Acme Corp");
        });

        System.out.println(list.getModel().getSize());
        System.out.println(combo.getSelectedItem());

        // Close adapters when the view/presenter lifecycle ends to detach
        // source listeners and avoid retaining obsolete Swing components.
        listModel.close();
        comboModel.close();
        swingCustomers.close();
    }
}
