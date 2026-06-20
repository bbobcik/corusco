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
 * Coordinates standard modal form-dialog semantics for a Swing view.
 *
 * <p>{@code FormDialog} is a controller, not a {@code JDialog}. It owns the
 * state machine behind OK, Apply, Revert, and Cancel for a Corusco
 * {@link FormModel}: active editor commit, committability checks, result
 * creation, dirty-cancel confirmation, baseline acceptance, form reset, and
 * command enablement. A generated presenter or handwritten screen can host the
 * root component in any modal shell while using this controller for the dialog
 * rules that should be consistent across screens.</p>
 *
 * <p>The controller sits between the toolkit-neutral form model and the Swing
 * helpers in this package. {@link FormDialogKeyboardBinding} installs root-pane
 * Enter/Escape behavior that delegates to this controller's commands. {@link
 * FormDialogValidationBinding} reads the controller's model problems and can
 * focus components through {@link ProblemFocusResolver}. {@link
 * FormDialogLifecycle} owns bindings, task services, and detachables that live
 * beside the dialog and closes the controller at the end of the view
 * lifecycle.</p>
 *
 * <p>Instances are mutable, Event Dispatch Thread confined, and represent one
 * dialog interaction. After OK, Cancel, or {@link #close()} closes the
 * controller, command execution is disabled and the terminal {@link
 * DialogResult} should be treated as final. Apply is intentionally non-terminal:
 * it creates a result snapshot, accepts the current form values as the new
 * baseline, and keeps the controller open. If the user later cancels, the
 * terminal result is accepted with the last applied value. Revert is terminal
 * only when an explicit {@link RevertPolicy} restores pre-dialog state.</p>
 *
 * <p>The controller retains the supplied form model, root component, dirty-state
 * hook, and cancellation hook. It does not own the native window, does not
 * install keyboard bindings, does not render validation summaries, does not
 * manage task services, and does not close ordinary component bindings by
 * itself. Register those resources with {@link FormDialogLifecycle} or another
 * view owner.</p>
 *
 * <p>Typical usage:</p>
 *
 * <pre>{@code
 * FormDialog<CustomerForm, Customer> dialog = new FormDialog<>(form, rootPanel);
 * FormDialogLifecycle lifecycle = FormDialogLifecycle.of(dialog);
 * lifecycle.addBinding(FormDialogKeyboardBinding.install(rootPane, dialog, okButton));
 * lifecycle.addBinding(FormDialogValidationBinding.install(dialog, summaryLabel));
 *
 * okButton.setAction(new SwingActionAdapter(dialog.okCommand(), resources));
 * }</pre>
 *
 * <p>Subclassing is supported only for narrow customization of active editor
 * commit through {@link #commitActiveEditor()}. Subclasses should preserve the
 * EDT confinement, one-shot close behavior, command-state invariants, and form
 * commit/reset semantics implemented by this class.</p>
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
     * Stable action key for Revert.
     */
    public static final ActionKey REVERT_KEY = ActionKey.of("dialog/revert");

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

    /**
     * Resource key for Revert button text.
     */
    public static final ResourceKey<String> REVERT_TEXT = ResourceKey.of("dialog/revert/text", String.class);

    private final P formModel;
    private final JComponent root;
    private final DirtyState dirtyState;
    private final CancelConfirmation cancelConfirmation;
    private final RevertPolicy revertPolicy;
    private final MutableCommand okCommand;
    private final MutableCommand applyCommand;
    private final MutableCommand cancelCommand;
    private final MutableCommand revertCommand;
    private DialogResult<R> result = DialogResult.cancelled();
    private R lastAppliedResult;
    private boolean hasAppliedResult;
    private boolean closed;

    /**
     * Creates a form dialog controller with no dirty-cancel confirmation.
     *
     * <p>The controller starts open, initializes OK and Apply enablement from
     * {@link FormModel#isCommittable()}, and uses {@link DirtyState#CLEAN} and
     * {@link CancelConfirmation#ALWAYS_CONFIRM}. The root component is retained
     * for active-editor commit before OK or Apply. Construction must happen on
     * the Event Dispatch Thread.</p>
     *
     * @param formModel form model retained by the controller, not {@code null}
     * @param root root component used for active-editor commit, not
     *         {@code null}
     * @throws IllegalStateException if called off the EDT
     * @throws NullPointerException if {@code formModel} or {@code root} is
     *         {@code null}
     */
    public FormDialog(P formModel, JComponent root) {
        this(formModel, root, DirtyState.CLEAN, CancelConfirmation.ALWAYS_CONFIRM);
    }

    /**
     * Creates a form dialog controller with explicit dirty-cancel policy.
     *
     * <p>The supplied hooks are retained and invoked only by user cancellation
     * through {@link #cancel()} or the cancel command. Programmatic
     * {@link #close()} forces cancellation cleanup without asking confirmation.
     * Construction initializes the three dialog commands and their initial
     * enabled state.</p>
     *
     * @param formModel form model retained by the controller, not {@code null}
     * @param root root component used for active-editor commit, not
     *         {@code null}
     * @param dirtyState aggregate dirty-state hook, not {@code null}
     * @param cancelConfirmation confirmation hook used for dirty cancellation,
     *         not {@code null}
     * @throws IllegalStateException if called off the EDT
     * @throws NullPointerException if any argument is {@code null}
     */
    public FormDialog(
            P formModel,
            JComponent root,
            DirtyState dirtyState,
            CancelConfirmation cancelConfirmation
    ) {
        this(formModel, root, dirtyState, cancelConfirmation, RevertPolicy.UNSUPPORTED);
    }

    /**
     * Creates a form dialog controller with explicit dirty-cancel and revert
     * policies.
     *
     * <p>Revert is optional and should be supplied only when the application can
     * restore pre-dialog state. It is separate from form reset because Apply
     * accepts the current form values as the normal reset baseline.</p>
     *
     * @param formModel form model retained by the controller, not {@code null}
     * @param root root component used for active-editor commit, not
     *         {@code null}
     * @param dirtyState aggregate dirty-state hook, not {@code null}
     * @param cancelConfirmation confirmation hook used for dirty cancellation,
     *         not {@code null}
     * @param revertPolicy optional pre-dialog restoration policy, not
     *         {@code null}
     * @throws IllegalStateException if called off the EDT
     * @throws NullPointerException if any argument is {@code null}
     */
    public FormDialog(
            P formModel,
            JComponent root,
            DirtyState dirtyState,
            CancelConfirmation cancelConfirmation,
            RevertPolicy revertPolicy
    ) {
        SwingEdt.requireEdt();
        this.formModel = Objects.requireNonNull(formModel, "formModel");
        this.root = Objects.requireNonNull(root, "root");
        this.dirtyState = Objects.requireNonNull(dirtyState, "dirtyState");
        this.cancelConfirmation = Objects.requireNonNull(cancelConfirmation, "cancelConfirmation");
        this.revertPolicy = Objects.requireNonNull(revertPolicy, "revertPolicy");
        this.okCommand = CommandFactory.command(ActionDescriptor.action(OK_KEY, OK_TEXT), command -> accept());
        this.applyCommand = CommandFactory.command(ActionDescriptor.action(APPLY_KEY, APPLY_TEXT), command -> apply());
        this.cancelCommand = CommandFactory.command(ActionDescriptor.action(CANCEL_KEY, CANCEL_TEXT), command -> cancel());
        this.revertCommand = CommandFactory.command(ActionDescriptor.action(REVERT_KEY, REVERT_TEXT), command -> revert());
        refreshCommandState();
    }

    /**
     * Returns the form model coordinated by this controller.
     *
     * @return form model, never {@code null}
     */
    public P formModel() {
        return formModel;
    }

    /**
     * Returns the root component inspected for active editor commit.
     *
     * @return root component, never {@code null}
     */
    public JComponent root() {
        return root;
    }

    /**
     * Returns the dirty-state hook used by user cancellation.
     *
     * @return dirty-state hook, never {@code null}
     */
    public DirtyState dirtyState() {
        return dirtyState;
    }

    /**
     * Returns the dirty-cancel confirmation hook.
     *
     * @return cancel confirmation hook, never {@code null}
     */
    public CancelConfirmation cancelConfirmation() {
        return cancelConfirmation;
    }

    /**
     * Returns the optional pre-dialog restoration policy.
     *
     * @return revert policy, never {@code null}
     */
    public RevertPolicy revertPolicy() {
        return revertPolicy;
    }

    /**
     * Returns the OK command.
     *
     * <p>Executing this command delegates to {@link #accept()}. Its enabled
     * state follows {@link #refreshCommandState()} and becomes disabled after
     * the controller closes.</p>
     *
     * @return OK command
     */
    public MutableCommand okCommand() {
        return okCommand;
    }

    /**
     * Returns the Apply command.
     *
     * <p>Executing this command delegates to {@link #apply()}. It is enabled
     * only while the controller is open and the form model is committable.</p>
     *
     * @return Apply command
     */
    public MutableCommand applyCommand() {
        return applyCommand;
    }

    /**
     * Returns the Cancel command.
     *
     * <p>Executing this command delegates to {@link #cancel()}. It is enabled
     * while the controller is open and disabled after terminal close.</p>
     *
     * @return Cancel command
     */
    public MutableCommand cancelCommand() {
        return cancelCommand;
    }

    /**
     * Returns the Revert command.
     *
     * <p>Executing this command delegates to {@link #revert()}. It is enabled
     * only while the controller is open and the configured revert policy
     * reports that restore is currently possible.</p>
     *
     * @return Revert command
     */
    public MutableCommand revertCommand() {
        return revertCommand;
    }

    /**
     * Returns the terminal dialog result.
     *
     * <p>The result is cancelled until OK accepts the dialog. Apply keeps the
     * controller open and records a last-applied value. If user Cancel follows
     * Apply, the terminal result becomes accepted with that last-applied value.
     * Revert records a reverted result when its policy succeeds. After OK,
     * Cancel, Revert, or {@link #close()}, the returned result represents the
     * final controller outcome.</p>
     *
     * @return dialog result, never {@code null}
     */
    public DialogResult<R> result() {
        return result;
    }

    /**
     * Returns the most recent successfully applied value.
     *
     * <p>This value is updated by {@link #apply()} only. OK stores its accepted
     * value in {@link #result()} instead.</p>
     *
     * @return optional applied value
     */
    public Optional<R> lastAppliedResult() {
        return hasAppliedResult ? Optional.of(lastAppliedResult) : Optional.empty();
    }

    /**
     * Indicates whether the controller has reached a terminal closed state.
     *
     * @return closed flag
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Refreshes dialog command enablement from current controller and form
     * state.
     *
     * <p>OK and Apply are enabled only while the controller is open and
     * {@link FormModel#isCommittable()} is {@code true}. Cancel is enabled only
     * while the controller is open. Call this after form validation state
     * changes if the form model does not expose observable command-state
     * updates.</p>
     *
     * @throws IllegalStateException if called off the EDT
     */
    public void refreshCommandState() {
        SwingEdt.requireEdt();
        boolean canCommit = !closed && formModel.isCommittable();
        okCommand.setEnabled(canCommit);
        applyCommand.setEnabled(canCommit);
        cancelCommand.setEnabled(!closed);
        revertCommand.setEnabled(!closed && revertPolicy.canRevert());
    }

    /**
     * Performs OK semantics and closes the controller when commit succeeds.
     *
     * <p>The method first asks the active editor under {@link #root()} to commit
     * through {@link #commitActiveEditor()}. If an editor rejects the commit, or
     * if the form is not committable after command-state refresh, the method
     * returns {@code false} and leaves the controller open. On success it calls
     * {@link FormModel#toResult()}, requires a non-null result, accepts the
     * current form values as the new baseline, stores an accepted
     * {@link DialogResult}, closes the controller, and disables all commands.</p>
     *
     * @return {@code true} when the dialog accepted and closed
     * @throws IllegalStateException if called off the EDT
     * @throws NullPointerException if {@link FormModel#toResult()} returns
     *         {@code null}
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
     * <p>Apply uses the same active-editor and committability checks as
     * {@link #accept()}. On success it stores the latest committed value in
     * {@link #lastAppliedResult()}, accepts the current form values as the new
     * baseline, and leaves the controller open. If the user later cancels the
     * dialog, that cancellation closes as accepted with this last applied value.
     * If the controller is closed, an editor rejects commit, or the form is not
     * committable, the method returns {@code false} without creating a result or
     * moving the applied baseline.</p>
     *
     * @return {@code true} when the form applied successfully
     * @throws IllegalStateException if called off the EDT
     * @throws NullPointerException if {@link FormModel#toResult()} returns
     *         {@code null}
     */
    public boolean apply() {
        SwingEdt.requireEdt();
        if (closed || !prepareCommit()) {
            return false;
        }
        lastAppliedResult = Objects.requireNonNull(formModel.toResult(), "formModel.toResult()");
        hasAppliedResult = true;
        formModel.acceptCurrentValues();
        refreshCommandState();
        return true;
    }

    /**
     * Performs user cancellation semantics.
     *
     * <p>If the controller is already closed, the method is idempotent and
     * returns {@code true}. Otherwise it asks {@link DirtyState#isDirty()} and,
     * when dirty, calls {@link CancelConfirmation#confirmCancel()}. A rejected
     * confirmation leaves the controller open and does not reset the form. A
     * confirmed or clean cancellation resets the form model and closes the
     * controller. If Apply succeeded earlier, the terminal result is accepted
     * with the last applied value; otherwise it is cancelled.</p>
     *
     * @return {@code true} when cancellation closed the controller or it was
     *         already closed; {@code false} when dirty confirmation rejected
     *         cancellation
     * @throws IllegalStateException if called off the EDT
     */
    public boolean cancel() {
        SwingEdt.requireEdt();
        if (closed) {
            return true;
        }
        if (!confirmCancelIfDirty()) {
            return false;
        }
        closeAfterUserCancellation();
        return true;
    }

    /**
     * Restores pre-dialog state through the configured revert policy.
     *
     * <p>Revert is terminal because the policy owns restoring the state that
     * existed before the dialog interaction began. The form model is not reset
     * here; reset only returns to the current baseline, which Apply is allowed
     * to move forward.</p>
     *
     * @return {@code true} when revert succeeded and closed the controller
     * @throws IllegalStateException if called off the EDT
     */
    public boolean revert() {
        SwingEdt.requireEdt();
        if (closed || !revertPolicy.canRevert()) {
            return false;
        }
        if (!revertPolicy.revert()) {
            refreshCommandState();
            return false;
        }
        lastAppliedResult = null;
        hasAppliedResult = false;
        result = DialogResult.reverted();
        closed = true;
        refreshCommandState();
        return true;
    }

    /**
     * Forces controller cleanup as a cancelled dialog.
     *
     * <p>This lifecycle method is idempotent and intentionally bypasses dirty
     * confirmation. Use {@link #cancel()} for user-initiated cancellation that
     * should respect dirty-state policy; use {@code close()} when an owning
     * shell or {@link FormDialogLifecycle} is disposing the dialog resources.</p>
     *
     * @throws IllegalStateException if called off the EDT
     */
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
     * <p>The default implementation delegates to
     * {@link SwingEditors#commitActiveEditor(JComponent)}. Subclasses may
     * override this method to integrate a custom editor framework or to test
     * rejected commits, but should not mutate dialog result state or command
     * enablement here. Returning {@code false} blocks both OK and Apply and
     * leaves the controller open.</p>
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

    private void closeAfterUserCancellation() {
        result = hasAppliedResult
                ? DialogResult.accepted(lastAppliedResult)
                : DialogResult.cancelled();
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
