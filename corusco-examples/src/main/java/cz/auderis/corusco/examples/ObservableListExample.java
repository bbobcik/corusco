package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.collection.ListChangeSet;
import cz.auderis.corusco.core.collection.ObservableArrayList;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates observable list changes.
 */
public final class ObservableListExample {

    private ObservableListExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a batched observable-list scenario.
     *
     * @return event and snapshot details
     */
    public static List<String> runScenario() {
        ObservableArrayList<String> rows = ObservableArrayList.empty();
        List<String> events = new ArrayList<>();
        rows.subscribe(changes -> events.add(describe(changes)));

        // Batch delivery keeps the exact inner mutation order, but observers
        // see one coherent change set. Swing adapters can translate that set
        // into precise ListModel events later.
        rows.batch(batch -> {
            batch.add("alpha");
            batch.add("beta");
            batch.set(1, "bravo");
        });
        rows.move(1, 0);

        List<String> result = new ArrayList<>(events);
        // Snapshots are immutable point-in-time views, so callers can inspect
        // list state without getting write access to internal storage.
        result.add(String.join(",", rows.snapshot()));
        return result;
    }

    private static String describe(ListChangeSet<String> changes) {
        return Integer.toString(changes.changes().size());
    }
}
