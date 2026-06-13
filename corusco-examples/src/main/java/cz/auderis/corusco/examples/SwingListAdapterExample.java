package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.collection.ObservableComboBoxModel;
import cz.auderis.corusco.swing.collection.ObservableListModel;
import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * Demonstrates Swing list adapters over one observable source list.
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
        ObservableListModel<String> listModel = ObservableListModel.of(customers);
        ObservableComboBoxModel<String> comboModel = ObservableComboBoxModel.of(customers);

        // Swing components consume adapters; application code keeps mutating
        // the observable list so all views see the same ordered data.
        JList<String> list = new JList<>(listModel);
        JComboBox<String> combo = new JComboBox<>(comboModel);
        comboModel.setSelectedItem("Acme");

        // Mutations must currently happen on the EDT while Swing adapters are
        // subscribed. A later dispatcher slice will cover background changes.
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
    }
}
