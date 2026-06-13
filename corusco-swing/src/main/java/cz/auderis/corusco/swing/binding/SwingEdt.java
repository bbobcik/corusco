package cz.auderis.corusco.swing.binding;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

/**
 * Small EDT helper for Swing bindings.
 */
public final class SwingEdt {

    private SwingEdt() {
    }

    /**
     * Fails unless the current thread is the Swing Event Dispatch Thread.
     */
    public static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Swing binding code must run on the EDT");
        }
    }

    /**
     * Runs work on the EDT and waits for completion.
     *
     * @param work work to run
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
}
