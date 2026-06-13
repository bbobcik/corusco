package cz.auderis.corusco.examples;

import ca.odell.glazedlists.BasicEventList;
import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.glazedlists.GlazedListsAdapters;
import cz.auderis.corusco.glazedlists.GlazedObservableList;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates adapting a Glazed Lists event list to Corusco.
 */
public final class GlazedListsInteropExample {

    private GlazedListsInteropExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a Glazed Lists interop scenario.
     *
     * @return event and snapshot details
     */
    public static List<String> runScenario() {
        BasicEventList<String> eventList = new BasicEventList<>(new ArrayList<>(List.of("alpha", "beta")));
        GlazedObservableList<String> observable = GlazedListsAdapters.observableList(eventList);
        List<String> result = new ArrayList<>();
        observable.subscribe(changes -> result.add(describe(changes)));

        // Existing Glazed Lists code can keep mutating the EventList directly;
        // the Corusco adapter observes those events and translates them.
        eventList.add("gamma");

        // Corusco code may also use the ObservableList facade. The wrapped
        // EventList remains the storage owner in both directions.
        observable.set(1, "bravo");
        result.add(String.join(",", observable.snapshot()));

        // The adapter is a listener attachment, not the EventList owner. Close
        // it when the Corusco-side lifecycle ends.
        observable.close();
        eventList.add("delta");
        result.add(String.join(",", eventList));
        return result;
    }

    private static String describe(ListChangeSet<String> changes) {
        return Integer.toString(changes.changes().size());
    }
}
