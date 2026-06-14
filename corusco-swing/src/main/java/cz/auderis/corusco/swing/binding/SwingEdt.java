package cz.auderis.corusco.swing.binding;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import javax.swing.SwingUtilities;

/**
 * Small utility facade for Swing Event Dispatch Thread boundaries.
 *
 * <p>Corusco Swing bindings, behaviors, dialogs, and test helpers use this
 * class to make EDT assumptions visible in code and Javadoc. It does not make
 * arbitrary Swing code thread-safe; it simply centralizes the common
 * operations used when component state must be read or mutated on the EDT.</p>
 *
 * <p>Use {@link #requireEdt()} at installation, mutation, and cleanup points
 * that must already be running in Swing. Use {@link #runAndWait(Runnable)}
 * from tests or non-EDT setup code when synchronous construction is required.
 * Use {@link #executor()} or {@link #runLater(Runnable)} for callback delivery
 * from background tasks. Do not use these helpers to hide long-running work on
 * the EDT; expensive work belongs in a task service.</p>
 */
public final class SwingEdt {

    private static final Executor EXECUTOR = SwingEdt::runLater;

    private SwingEdt() {
    }

    /**
     * Returns an executor that queues work on the Swing Event Dispatch Thread.
     *
     * <p>The executor uses {@link #runLater(Runnable)} and therefore returns
     * immediately after scheduling the task.</p>
     *
     * @return EDT executor
     */
    public static Executor executor() {
        return EXECUTOR;
    }

    /**
     * Fails unless the current thread is the Swing Event Dispatch Thread.
     *
     * @throws IllegalStateException when invoked off the EDT
     */
    public static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Swing binding code must run on the EDT");
        }
    }

    /**
     * Runs work on the EDT and waits for completion.
     *
     * <p>If already on the EDT, the runnable executes immediately. If the
     * waiting thread is interrupted, its interrupt status is restored and the
     * failure is wrapped in {@link IllegalStateException}. Exceptions thrown by
     * the runnable are also wrapped.</p>
     *
     * @param work work to run, not {@code null}
     * @throws IllegalStateException if dispatch is interrupted or the work
     *         fails
     */
    public static void runAndWait(Runnable work) {
        if (SwingUtilities.isEventDispatchThread()) {
            work.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(work);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for EDT", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("EDT work failed", e.getCause());
        }
    }

    /**
     * Queues work on the EDT and returns immediately.
     *
     * <p>The runnable is retained by Swing until dispatch. Exceptions thrown by
     * the runnable follow Swing's normal asynchronous exception handling rather
     * than being reported to the caller of this method.</p>
     *
     * @param work work to run, not {@code null}
     */
    public static void runLater(Runnable work) {
        SwingUtilities.invokeLater(work);
    }
}
