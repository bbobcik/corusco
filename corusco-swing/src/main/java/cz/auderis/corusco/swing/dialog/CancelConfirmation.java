package cz.auderis.corusco.swing.dialog;

/**
 * Confirmation hook used before discarding dirty dialog edits.
 *
 * <p>The hook is UI-policy neutral. Applications can connect it to
 * {@code JOptionPane}, a custom dialog, or tests. It is invoked on the EDT by
 * {@link FormDialog}.</p>
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
