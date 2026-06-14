package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.Objects;
import javax.swing.Timer;

/**
 * EDT-confined debounce helper for table-state saves.
 *
 * <p>{@link TableStateController} uses this scheduler to coalesce repeated
 * column move, resize, visibility, and sort changes into one save action. A
 * zero delay runs saves immediately; a positive delay uses a non-repeating
 * Swing {@link Timer}. The scheduler does not know how state is captured or
 * stored; it only owns pending-save timing.</p>
 */
final class TableStateSaveScheduler {

    private final Runnable saveAction;
    private final Timer timer;
    private boolean pending;

    TableStateSaveScheduler(int delayMillis, Runnable saveAction) {
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must not be negative");
        }
        this.saveAction = Objects.requireNonNull(saveAction, "saveAction");
        this.timer = new Timer(delayMillis, event -> runPendingSave());
        this.timer.setRepeats(false);
    }

    void schedule() {
        SwingEdt.requireEdt();
        pending = true;
        if (timer.getInitialDelay() == 0) {
            runPendingSave();
        } else {
            timer.restart();
        }
    }

    void saveNow() {
        SwingEdt.requireEdt();
        pending = false;
        timer.stop();
        saveAction.run();
    }

    void flushPending() {
        SwingEdt.requireEdt();
        if (!pending) {
            return;
        }
        runPendingSave();
    }

    private void runPendingSave() {
        SwingEdt.requireEdt();
        if (!pending) {
            return;
        }
        pending = false;
        timer.stop();
        saveAction.run();
    }
}
