package cz.auderis.corusco.swing.dialog;

/**
 * Supplies aggregate dirty state for a form dialog.
 *
 * <p>{@link FormDialog} uses this hook before user cancellation to decide
 * whether {@link CancelConfirmation} should be consulted. It is intentionally
 * separate from {@link cz.auderis.corusco.core.form.FormModel} because
 * applications may define dirty state from a subset of fields, nested editors,
 * table changes, or external presenter state.</p>
 *
 * <p>Generated code should compose the relevant field dirty values explicitly
 * instead of relying on reflection or property paths. Implementations should be
 * cheap and side-effect free because they may be queried whenever the user
 * attempts to close or cancel the dialog, on the Event Dispatch Thread.</p>
 */
@FunctionalInterface
public interface DirtyState {

    /**
     * Dirty state that never blocks cancellation.
     */
    DirtyState CLEAN = () -> false;

    /**
     * Indicates whether the dialog contains unsaved user changes.
     *
     * @return {@code true} when cancellation should ask confirmation
     */
    boolean isDirty();
}
