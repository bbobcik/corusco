package cz.auderis.corusco.examples.collections;

import cz.auderis.corusco.core.collection.ListChange;
import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.collection.EdtObservableList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

/**
 * Demonstrates marshaling observable-list notifications to the Swing EDT.
 *
 * <p>The example adapts list events so Swing-facing consumers receive changes
 * on the event dispatch thread. It clarifies the boundary between the core
 * observable-list contract and Swing's threading expectations.</p>
 */
public final class EdtObservableListExample {

    private EdtObservableListExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a small background-update scenario.
     *
     * @return diagnostics describing listener thread and final contents
     */
    public static List<String> runScenario() {
        ObservableArrayList<String> source = ObservableArrayList.of(List.of("draft"));
        EdtObservableList<String> edtList = EdtObservableList.of(source);
        List<String> result = new ArrayList<>();
        CountDownLatch delivered = new CountDownLatch(1);

        SwingEdt.runAndWait(() -> edtList.subscribe(changes -> {
            // Subscribers attached to the wrapper can update Swing components
            // directly because the wrapper marshals this callback to the EDT.
            result.add(Boolean.toString(SwingUtilities.isEventDispatchThread()));
            result.add(describe(changes.changes().getFirst()));
            delivered.countDown();
        }));

        // The source list is still the storage owner. Background work mutates
        // that source; the wrapper only controls where listeners are notified.
        Thread worker = new Thread(() -> source.add("approved"), "corusco-edt-list-example");
        worker.start();
        join(worker);
        await(delivered);

        SwingEdt.runAndWait(() -> result.add(String.join(",", edtList.snapshot())));

        // Close the wrapper with the view/presenter lifecycle so queued
        // callbacks cannot retain obsolete Swing components.
        edtList.close();
        return List.copyOf(result);
    }

    private static String describe(ListChange<String> change) {
        return switch (change) {
            case ListChange.Inserted<String> inserted -> "inserted:" + inserted.index() + ":" + inserted.elements();
            case ListChange.Removed<String> removed -> "removed:" + removed.index() + ":" + removed.elements();
            case ListChange.Replaced<String> replaced -> "replaced:" + replaced.index();
            case ListChange.Moved<String> moved -> "moved:" + moved.fromIndex() + ":" + moved.toIndex();
            case ListChange.Cleared<String> cleared -> "cleared:" + cleared.elements().size();
        };
    }

    private static void join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for background list update", e);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for EDT list delivery");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for EDT list delivery", e);
        }
    }
}
