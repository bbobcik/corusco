package cz.auderis.corusco.swing.dialog;

/**
 * Confirmation hook used before a {@link FormDialog} discards dirty edits.
 *
 * <p>This interface represents application policy, not a specific UI widget.
 * Implementations can show a {@code JOptionPane}, delegate to an application
 * dialog service, consult presenter state, or record a test decision. The hook
 * is invoked on the Event Dispatch Thread by {@link FormDialog#cancel()} when
 * {@link DirtyState#isDirty()} reports dirty state.</p>
 *
 * <p>Returning {@code true} allows cancellation to reset the form and close the
 * controller. Returning {@code false} leaves the controller open and leaves the
 * form untouched. {@link FormDialog#close()} bypasses this hook because it is a
 * lifecycle cleanup operation rather than user cancellation.</p>
 */
@FunctionalInterface
public interface CancelConfirmation {

    /**
     * Confirmation that always allows cancellation.
     */
    CancelConfirmation ALWAYS_CONFIRM = () -> true;

    /**
     * Asks whether dirty edits may be discarded.
     *
     * @return {@code true} to continue cancellation
     */
    boolean confirmCancel();
}
