package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.collection.LoadableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates a detachable observable row list.
 */
public final class LoadableListExample {

    private LoadableListExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a lazy row-list scenario.
     *
     * @return diagnostics describing list loading and detaching
     */
    public static List<String> runScenario() {
        AtomicInteger loads = new AtomicInteger();
        LoadableList<String> rows = LoadableList.of(() -> List.of("row-" + loads.incrementAndGet()));
        List<String> result = new ArrayList<>();

        // Presenter construction can create the row model without loading.
        // The first table/list adapter access is what attaches the cache.
        result.add("attached=" + rows.isAttached());
        result.add(String.join(",", rows.snapshot()));
        result.add(String.join(",", rows.snapshot()));

        // Local mutations operate on the attached presentation cache and still
        // emit ordinary ObservableList changes to Swing adapters.
        rows.add("local");
        result.add(String.join(",", rows.snapshot()));

        // Detach releases cached rows but keeps the LoadableList object and
        // its external subscribers reusable for the next view activation.
        rows.detach();
        result.add("attached=" + rows.isAttached());
        result.add(String.join(",", rows.snapshot()));
        result.add("loads=" + loads.get());
        return result;
    }
}
