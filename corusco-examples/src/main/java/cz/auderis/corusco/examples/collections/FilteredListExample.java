package cz.auderis.corusco.examples.collections;

import cz.auderis.corusco.core.collection.FilteredList;
import cz.auderis.corusco.core.collection.ObservableArrayList;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates a filtered observable-list view.
 *
 * <p>The scenario derives a visible list from a source observable list and
 * updates it when source data or filter criteria change. It shows how list
 * adapters keep derived presentation state separate from storage ownership.</p>
 */
public final class FilteredListExample {

    private FilteredListExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a small filtered-list scenario.
     *
     * @return filtered snapshots after each relevant step
     */
    public static List<String> runScenario() {
        ObservableArrayList<String> source = ObservableArrayList.of(List.of("alpha", "beta", "apricot"));
        FilteredList<String> filtered = FilteredList.of(source, value -> value.startsWith("a"));
        List<String> snapshots = new ArrayList<>();

        // The source list remains the only mutation owner. The filtered view
        // listens and translates source indices into visible-view indices.
        snapshots.add(String.join(",", filtered.snapshot()));
        source.add("atlas");
        source.set(1, "avocado");
        snapshots.add(String.join(",", filtered.snapshot()));

        // Predicate replacement is useful for search boxes. The current
        // FilteredList contract reports it as a reset.
        filtered.setPredicate(value -> value.length() > 5);
        snapshots.add(String.join(",", filtered.snapshot()));

        // Close transformed views with the presenter/view lifecycle so source
        // lists do not retain obsolete listeners.
        filtered.close();
        source.add("anchovy");
        snapshots.add(String.join(",", filtered.snapshot()));
        return snapshots;
    }
}
