package cz.auderis.corusco.swing.task;

import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.swing.binding.SwingEdt;

/**
 * Factory helpers for task services whose UI callbacks are delivered on Swing's EDT.
 *
 * <p>The core task package is toolkit-neutral: it knows how to run work away
 * from the caller and how to notify a configured callback executor. This class
 * supplies the Swing-specific executor choice so presenters can submit
 * background work without manually wiring {@link javax.swing.SwingUtilities}
 * at every call site. Background task bodies still run off the UI thread; only
 * terminal callbacks and final busy-state notifications are posted through
 * {@link SwingEdt#executor()}.</p>
 *
 * <p>The returned services are lifecycle objects. They should be closed by the
 * owning application, presenter, or dialog scope when no more work should be
 * accepted. Do not run long calculations in the EDT callbacks themselves; use
 * callbacks to update models or components with the already-produced result.</p>
 */
public final class SwingTaskServices {

    private SwingTaskServices() {
        throw new AssertionError("No instances");
    }

    /**
     * Creates a virtual-thread-backed task service whose terminal callbacks and
     * final busy-state updates are delivered on the Swing EDT.
     *
     * <p>The returned service owns its worker executor. Callers should close the
     * service, or register it with a dialog/view lifecycle, when no more tasks
     * should be accepted.</p>
     *
     * @return Swing task service
     */
    public static TaskService virtualThreads() {
        return TaskService.virtualThreads(SwingEdt.executor());
    }
}
