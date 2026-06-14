package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.task.TaskCallbacks;
import cz.auderis.corusco.core.task.TaskHandle;
import cz.auderis.corusco.core.task.TaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Demonstrates core asynchronous task execution without Swing.
 *
 * <p>The example submits a task through the core task service and observes
 * completion through the returned handle. It focuses on cancellation and
 * terminal-result flow before Swing-specific callback dispatch is introduced.</p>
 */
public final class AsyncTaskExample {

    private AsyncTaskExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a small async task scenario.
     *
     * @return diagnostics describing busy state and callback delivery
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        CountDownLatch release = new CountDownLatch(1);
        try (TaskService service = TaskService.virtualThreads(Runnable::run)) {
            service.busy().subscribe(event -> result.add("serviceBusy=" + event.newValue()));

            // The core service accepts a callback executor. This example uses a
            // direct executor for deterministic tests; the Swing layer can pass
            // an EDT executor without changing task bodies.
            TaskHandle<String> handle = service.submit(
                    cancellation -> {
                        cancellation.throwIfCancellationRequested();
                        release.await();
                        return "loaded";
                    },
                    TaskCallbacks.onSuccess(value -> result.add("success=" + value))
            );

            // The handle exposes task-level busy state and a completion future
            // that settles after callbacks have run.
            result.add("taskBusy=" + handle.busy().value());
            release.countDown();
            result.add("result=" + handle.completion().join());
            result.add("taskBusy=" + handle.busy().value());
        }
        return result;
    }
}
