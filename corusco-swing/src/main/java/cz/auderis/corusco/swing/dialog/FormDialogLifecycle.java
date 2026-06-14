package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.lifecycle.Detachable;
import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.SubscriptionScope;
import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.util.Objects;

/**
 * Collects everything that should be torn down with one form dialog instance.
 *
 * <p>{@code FormDialogLifecycle} is the ownership object that sits beside a
 * {@link FormDialog}. A generated or hand-written dialog presenter can register
 * field bindings, validation bindings, keyboard bindings, background task
 * services, and detachable presenter state in this lifecycle instead of
 * scattering cleanup code across the modal shell. The lifecycle does not show
 * or dispose a {@code JDialog}; it owns resources related to the dialog
 * controller and closes the controller last.</p>
 *
 * <p>Instances are EDT-confined. Create the lifecycle, add resources, and call
 * {@link #close()} on the Swing Event Dispatch Thread. Registered resources are
 * closed in reverse registration order through a
 * {@link cz.auderis.corusco.core.lifecycle.SubscriptionScope}. If the lifecycle
 * is already closed, late registrations are immediately closed or detached by
 * that scope, so callers do not need a separate "is this dialog still alive?"
 * branch during teardown.</p>
 *
 * <p>A typical presenter creates the dialog controller, wraps it in a
 * lifecycle, then registers every binding it installs:</p>
 *
 * <pre>{@code
 * FormDialog<MyForm, Result> dialog = new FormDialog<>(form, root);
 * FormDialogLifecycle lifecycle = FormDialogLifecycle.of(dialog);
 * lifecycle.addBinding(FormDialogKeyboardBinding.install(rootPane, dialog, okButton));
 * lifecycle.addBinding(FormDialogValidationBinding.install(dialog, summaryLabel));
 * }</pre>
 */
public final class FormDialogLifecycle implements Binding {

    private final FormDialog<?, ?> dialog;
    private final SubscriptionScope resources = new SubscriptionScope();
    private boolean closed;

    private FormDialogLifecycle(FormDialog<?, ?> dialog) {
        SwingEdt.requireEdt();
        this.dialog = Objects.requireNonNull(dialog, "dialog");
    }

    /**
     * Creates a lifecycle owner for a dialog controller.
     *
     * <p>The controller is retained strongly until this lifecycle is closed.
     * The lifecycle will call {@link FormDialog#close()} after registered
     * resources have been closed.</p>
     *
     * @param dialog dialog controller to close with the lifecycle
     * @return lifecycle owner
     * @throws NullPointerException when {@code dialog} is {@code null}
     * @throws IllegalStateException when called off the EDT
     */
    public static FormDialogLifecycle of(FormDialog<?, ?> dialog) {
        return new FormDialogLifecycle(dialog);
    }

    /**
     * Returns the owned dialog controller.
     *
     * @return dialog controller
     */
    public FormDialog<?, ?> dialog() {
        return dialog;
    }

    /**
     * Registers a Swing binding owned by this dialog lifecycle.
     *
     * <p>The binding is retained strongly and closed when this lifecycle
     * closes. If this lifecycle is already closed, the binding is closed
     * immediately and is not retained.</p>
     *
     * @param binding binding to close with the dialog
     * @param <B> binding type
     * @return the same binding for fluent setup
     * @throws NullPointerException when {@code binding} is {@code null}
     * @throws IllegalStateException when called off the EDT
     */
    public <B extends Binding> B addBinding(B binding) {
        SwingEdt.requireEdt();
        return resources.add(binding);
    }

    /**
     * Registers a disposable resource owned by this dialog lifecycle.
     *
     * <p>Use this for resources that are not Swing bindings but still have the
     * same modal-dialog lifetime, such as listener handles or presenter-owned
     * helper services. Cleanup follows the same reverse-order and late-close
     * rules as {@link #addBinding(Binding)}.</p>
     *
     * @param disposable disposable to close with the dialog
     * @param <D> disposable type
     * @return the same disposable for fluent setup
     * @throws NullPointerException when {@code disposable} is {@code null}
     * @throws IllegalStateException when called off the EDT
     */
    public <D extends Disposable> D addDisposable(D disposable) {
        SwingEdt.requireEdt();
        return resources.add(disposable);
    }

    /**
     * Registers a task service owned by this dialog lifecycle.
     *
     * <p>The task service is treated as a disposable resource. Closing the
     * lifecycle delegates to the service's own close semantics; this class does
     * not cancel individual tasks directly.</p>
     *
     * @param taskService task service to close with the dialog
     * @param <T> task service type
     * @return the same task service for fluent setup
     * @throws NullPointerException when {@code taskService} is {@code null}
     * @throws IllegalStateException when called off the EDT
     */
    public <T extends TaskService> T addTaskService(T taskService) {
        return addDisposable(taskService);
    }

    /**
     * Registers a detachable presenter or model resource.
     *
     * <p>Detachable resources are converted to cleanup callbacks that call
     * {@link Detachable#detach()} during lifecycle close. This is intended for
     * reusable presentation state that should release cached data when the
     * dialog ends, not for listener subscriptions; listener subscriptions
     * should be registered as {@link Binding} or {@link Disposable} instances.</p>
     *
     * @param detachable detachable to detach with the dialog
     * @param <D> detachable type
     * @return the same detachable for fluent setup
     * @throws NullPointerException when {@code detachable} is {@code null}
     * @throws IllegalStateException when called off the EDT
     */
    public <D extends Detachable> D addDetachable(D detachable) {
        Objects.requireNonNull(detachable, "detachable");
        SwingEdt.requireEdt();
        resources.add(detachable::detach);
        return detachable;
    }

    /**
     * Indicates whether this lifecycle has been closed.
     *
     * @return {@code true} after close has started or the underlying resource
     *         scope has been closed
     */
    public boolean isClosed() {
        return closed || resources.isClosed();
    }

    /**
     * Closes registered resources and then closes the dialog controller.
     *
     * <p>The method is idempotent. Resource cleanup is attempted before
     * {@link FormDialog#close()} so component bindings are removed before the
     * form model is reset by dialog cancellation semantics. If resource cleanup
     * throws, the dialog controller is still closed from the {@code finally}
     * block.</p>
     *
     * @throws IllegalStateException when called off the EDT
     */
    @Override
    public void close() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        closed = true;
        try {
            resources.close();
        } finally {
            dialog.close();
        }
    }
}
