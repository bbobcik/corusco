package cz.auderis.corusco.swing.task;

import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.swing.binding.SwingEdt;

/**
 * Factory helpers for Swing-aware task services.
 */
public final class SwingTaskServices {

    private SwingTaskServices() {
        throw new AssertionError("No instances");
    }

    /**
     * Creates a virtual-thread-backed task service whose terminal callbacks and
     * final busy-state updates are delivered on the Swing EDT.
     *
     * @return Swing task service
     */
    public static TaskService virtualThreads() {
        return TaskService.virtualThreads(SwingEdt.executor());
    }
}
