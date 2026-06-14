package cz.auderis.corusco.examples.swing;

import cz.auderis.corusco.core.task.TaskCallbacks;
import cz.auderis.corusco.core.task.TaskHandle;
import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.swing.task.SwingTaskServices;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

/**
 * Demonstrates Swing EDT task callbacks.
 *
 * <p>The example submits background work through a Swing-aware task service and
 * observes completion on the event dispatch thread. It shows where UI updates
 * belong when task bodies run away from Swing components.</p>
 */
public final class SwingTaskExample {

    private SwingTaskExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a task and records where the task body and callback execute.
     *
     * @return task/callback diagnostics
     */
    public static List<String> runScenario() {
        List<String> result = new CopyOnWriteArrayList<>();
        try (TaskService service = SwingTaskServices.virtualThreads()) {
            TaskHandle<String> handle = service.submit(
                    cancellation -> {
                        // Blocking work runs on a virtual worker thread, so it
                        // can wait for I/O without freezing the Swing event
                        // queue.
                        result.add("taskEdt=" + SwingUtilities.isEventDispatchThread());
                        return "loaded";
                    },
                    TaskCallbacks.onSuccess(value -> {
                        // UI updates belong in callbacks because the Swing
                        // factory delivers them on the EDT.
                        result.add("callbackEdt=" + SwingUtilities.isEventDispatchThread());
                        result.add("value=" + value);
                    })
            );
            handle.completion().get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Task example failed", e);
        }
        return result;
    }
}
