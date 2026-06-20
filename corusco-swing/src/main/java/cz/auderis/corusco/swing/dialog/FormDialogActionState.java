package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.form.ComponentStateModel;
import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.Objects;

/**
 * Presentation models for dialog action controls.
 *
 * <p>The action state is presentation-only. It is useful for generated or
 * handwritten presenters that want Apply and Revert buttons to follow explicit
 * dirty policies without committing those button states into form results.</p>
 */
public final class FormDialogActionState {

    private final FormDialog<? extends FormModel<?>, ?> dialog;
    private final DirtyState currentBaselineDirty;
    private final DirtyState preDialogDirty;
    private final ComponentStateModel applyAction = new ComponentStateModel();
    private final ComponentStateModel revertAction = new ComponentStateModel();

    /**
     * Creates action presentation state.
     *
     * @param dialog dialog controller
     * @param currentBaselineDirty dirty state relative to current baseline
     * @param preDialogDirty dirty state relative to pre-dialog state
     */
    public FormDialogActionState(
            FormDialog<? extends FormModel<?>, ?> dialog,
            DirtyState currentBaselineDirty,
            DirtyState preDialogDirty
    ) {
        this.dialog = Objects.requireNonNull(dialog, "dialog");
        this.currentBaselineDirty = Objects.requireNonNull(currentBaselineDirty, "currentBaselineDirty");
        this.preDialogDirty = Objects.requireNonNull(preDialogDirty, "preDialogDirty");
        refresh();
    }

    /**
     * Returns Apply action presentation state.
     *
     * @return apply action component state
     */
    public ComponentStateModel applyAction() {
        return applyAction;
    }

    /**
     * Returns Revert action presentation state.
     *
     * @return revert action component state
     */
    public ComponentStateModel revertAction() {
        return revertAction;
    }

    /**
     * Refreshes action enablement.
     */
    public void refresh() {
        SwingEdt.requireEdt();
        applyAction.enabled().setValue(!dialog.isClosed()
                && dialog.formModel().isCommittable()
                && currentBaselineDirty.isDirty());
        revertAction.enabled().setValue(!dialog.isClosed()
                && dialog.revertPolicy().canRevert()
                && preDialogDirty.isDirty());
    }
}
