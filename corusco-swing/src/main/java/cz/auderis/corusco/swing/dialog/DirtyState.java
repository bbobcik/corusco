package cz.auderis.corusco.swing.dialog;

/**
 * Supplies aggregate dirty state for a dialog form.
 *
 * <p>The dialog controller asks this hook before cancellation. Generated code
 * should compose the relevant field dirty values explicitly instead of relying
 * on reflection or property paths.</p>
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
