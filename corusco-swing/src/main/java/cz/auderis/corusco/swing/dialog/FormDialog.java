package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.command.ActionDescriptor;
import cz.auderis.corusco.core.command.CommandFactory;
import cz.auderis.corusco.core.command.MutableCommand;
import cz.auderis.corusco.core.dialog.DialogResult;
import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.binding.SwingEditors;

import java.util.Objects;
import java.util.Optional;
import javax.swing.JComponent;

/**
 * EDT-bound controller for a modal form dialog lifecycle.
 *
 * <p>This class owns dialog command semantics but deliberately does not create
 * or show a native {@code JDialog}. Generated presenters and hand-written
 * screens can host the root component in any modal shell while reusing the
 * same OK, Apply, Cancel, active-editor commit, and result rules.</p>
 *
 * @param <P> form model type
 * @param <R> committed result type
 */
public class FormDialog<P extends FormModel<R>, R> implements Binding {

    /**
     * Stable action key for OK.
     */
    public static final ActionKey OK_KEY = ActionKey.of("dialog/ok");

    /**
     * Stable action key for Apply.
     */
    public static final ActionKey APPLY_KEY = ActionKey.of("dialog/apply");

    /**
     * Stable action key for Cancel.
     */
    public static final ActionKey CANCEL_KEY = ActionKey.of("dialog/cancel");

    /**
     * Resource key for OK button text.
     */
    public static final ResourceKey<String> OK_TEXT = ResourceKey.of("dialog/ok/text", String.class);

    /**
     * Resource key for Apply button text.
     */
    public static final ResourceKey<String> APPLY_TEXT = ResourceKey.of("dialog/apply/text", String.class);

    /**
     * Resource key for Cancel button text.
     */
    public static final ResourceKey<String> CANCEL_TEXT = ResourceKey.of("dialog/cancel/text", String.class);

    private final P formModel;
    private final JComponent root;
    private final DirtyState dirtyState;
    private final CancelConfirmation cancelConfirmation;
    private final MutableCommand okCommand;
    private final MutableCommand applyCommand;
    private final MutableCommand cancelCommand;
    private DialogResult<R> result = DialogResult.cancelled();
    private R lastAppliedResult;
    private boolean closed;

    /**
     * Creates a form dialog controller.
     *
     * @param formModel form model
     * @param root root component used for active-editor commit
     */
    public FormDialog(P formModel, JComponent root) {
        this(formModel, root, DirtyState.CLEAN, CancelConfirmation.ALWAYS_CONFIRM);
    }

    /**
     * Creates a form dialog controller.
     *
     * @param formModel form model
     * @param root root component used for active-editor commit
     * @param dirtyState aggregate dirty-state hook
     * @param cancelConfirmation confirmation hook used for dirty cancellation
     */
    public FormDialog(
            P formModel,
            JComponent root,
            DirtyState dirtyState,
            CancelConfirmation cancelConfirmation
    ) {
        SwingEdt.requireEdt();
        this.formModel = Objects.requireNonNull(formModel, "formModel");
        this.root = Objects.requireNonNull(root, "root");
        this.dirtyState = Objects.requireNonNull(dirtyState, "dirtyState");
        this.cancelConfirmation = Objects.requireNonNull(cancelConfirmation, "cancelConfirmation");
        this.okCommand = CommandFactory.command(ActionDescriptor.action(OK_KEY, OK_TEXT), command -> accept());
        this.applyCommand = CommandFactory.command(ActionDescriptor.action(APPLY_KEY, APPLY_TEXT), command -> apply());
        this.cancelCommand = CommandFactory.command(ActionDescriptor.action(CANCEL_KEY, CANCEL_TEXT), command -> cancel());
        refreshCommandState();
    }

    /**
     * Returns the form model owned by this controller.
     *
     * @return form model
     */
    public P formModel() {
        return formModel;
    }

    /**
     * Returns the root component inspected before commit.
     *
     * @return root component
     */
    public JComponent root() {
        return root;
    }

    /**
     * Returns the dirty-state hook.
     *
     * @return dirty-state hook
     */
    public DirtyState dirtyState() {
        return dirtyState;
    }

    /**
     * Returns the dirty-cancel confirmation hook.
     *
     * @return cancel confirmation hook
     */
    public CancelConfirmation cancelConfirmation() {
        return cancelConfirmation;
    }

    /**
     * Returns OK command.
     *
     * @return OK command
     */
    public MutableCommand okCommand() {
        return okCommand;
    }

    /**
     * Returns Apply command.
     *
     * @return Apply command
     */
    public MutableCommand applyCommand() {
        return applyCommand;
    }

    /**
     * Returns Cancel command.
     *
     * @return Cancel command
     */
    public MutableCommand cancelCommand() {
        return cancelCommand;
    }

    /**
     * Returns the terminal result. Until OK or Cancel closes the controller,
     * this is cancelled by default.
     *
     * @return dialog result
     */
    public DialogResult<R> result() {
        return result;
    }

    /**
     * Returns the most recent successfully applied value.
     *
     * @return optional applied value
     */
    public Optional<R> lastAppliedResult() {
        return Optional.ofNullable(lastAppliedResult);
    }

    /**
     * Indicates whether the controller is closed.
     *
     * @return closed flag
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Refreshes OK and Apply command enablement from current committability.
     */
    public void refreshCommandState() {
        SwingEdt.requireEdt();
        boolean canCommit = !closed && formModel.isCommittable();
        okCommand.setEnabled(canCommit);
        applyCommand.setEnabled(canCommit);
        cancelCommand.setEnabled(!closed);
    }

    /**
     * Performs OK semantics: commit editor, validate committability, create the
     * result, accept baselines, and close the controller.
     *
     * @return {@code true} when the dialog accepted
     */
    public boolean accept() {
        SwingEdt.requireEdt();
        if (closed || !prepareCommit()) {
            return false;
        }
        R committed = Objects.requireNonNull(formModel.toResult(), "formModel.toResult()");
        DialogResult<R> accepted = DialogResult.accepted(committed);
        formModel.acceptCurrentValues();
        result = accepted;
        closed = true;
        refreshCommandState();
        return true;
    }

    /**
     * Performs Apply semantics without closing the controller.
     *
     * @return {@code true} when the form applied
     */
    public boolean apply() {
        SwingEdt.requireEdt();
        if (closed || !prepareCommit()) {
            return false;
        }
        lastAppliedResult = Objects.requireNonNull(formModel.toResult(), "formModel.toResult()");
        formModel.acceptCurrentValues();
        refreshCommandState();
        return true;
    }

    /**
     * Performs Cancel semantics and closes the controller.
     *
     * @return {@code true} when cancellation closed the controller
     */
    public boolean cancel() {
        SwingEdt.requireEdt();
        if (closed) {
            return true;
        }
        if (!confirmCancelIfDirty()) {
            return false;
        }
        closeCancelled();
        return true;
    }

    @Override
    public void close() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        closeCancelled();
    }

    /**
     * Commits the active editor inside the dialog root.
     *
     * @return {@code true} when no editor rejected the commit
     */
    protected boolean commitActiveEditor() {
        return SwingEditors.commitActiveEditor(root);
    }

    private boolean confirmCancelIfDirty() {
        return !dirtyState.isDirty() || cancelConfirmation.confirmCancel();
    }

    private void closeCancelled() {
        result = DialogResult.cancelled();
        formModel.reset();
        closed = true;
        refreshCommandState();
    }

    private boolean prepareCommit() {
        if (!commitActiveEditor()) {
            return false;
        }
        refreshCommandState();
        return formModel.isCommittable();
    }
}
