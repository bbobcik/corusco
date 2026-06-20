package cz.auderis.corusco.swing.dialog;

/**
 * Optional policy for restoring pre-dialog state.
 *
 * <p>Revert is deliberately separate from
 * {@link cz.auderis.corusco.core.form.FormModel#reset()}, which resets to the
 * current form baseline. After Apply, that baseline normally moves forward.
 * A revert policy should only be supplied when the application can actually
 * restore both unapplied edits and already-applied dialog changes.</p>
 */
public interface RevertPolicy {

    /**
     * Revert policy for dialogs that do not support pre-dialog restoration.
     */
    RevertPolicy UNSUPPORTED = new RevertPolicy() {
        @Override
        public boolean canRevert() {
            return false;
        }

        @Override
        public boolean revert() {
            return false;
        }
    };

    /**
     * Indicates whether revert is currently available.
     *
     * @return {@code true} when {@link #revert()} may be attempted
     */
    boolean canRevert();

    /**
     * Restores pre-dialog state.
     *
     * @return {@code true} when restoration succeeded and the dialog may close
     */
    boolean revert();
}
