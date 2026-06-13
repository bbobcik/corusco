package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.lifecycle.Detachable;
import cz.auderis.corusco.core.lifecycle.Disposable;
import cz.auderis.corusco.core.lifecycle.SubscriptionScope;
import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.util.Objects;

/**
 * Owns resources for one form-dialog lifecycle.
 *
 * <p>The lifecycle is an EDT-bound owner for non-null bindings, presenter
 * detachables, task services, and other disposables created for one modal
 * dialog instance. All methods must be called on the Swing EDT. Closing
 * releases registered resources in reverse registration order and then closes
 * the dialog controller. Late registrations fail closed.</p>
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
     * Creates a lifecycle owner for a dialog.
     *
     * @param dialog dialog controller
     * @return lifecycle owner
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
     * @param binding binding to close with the dialog
     * @param <B> binding type
     * @return the same binding
     */
    public <B extends Binding> B addBinding(B binding) {
        SwingEdt.requireEdt();
        return resources.add(binding);
    }

    /**
     * Registers a disposable resource owned by this dialog lifecycle.
     *
     * @param disposable disposable to close with the dialog
     * @param <D> disposable type
     * @return the same disposable
     */
    public <D extends Disposable> D addDisposable(D disposable) {
        SwingEdt.requireEdt();
        return resources.add(disposable);
    }

    /**
     * Registers a task service owned by this dialog lifecycle.
     *
     * @param taskService task service to close with the dialog
     * @param <T> task service type
     * @return the same task service
     */
    public <T extends TaskService> T addTaskService(T taskService) {
        return addDisposable(taskService);
    }

    /**
     * Registers a detachable presenter/model resource.
     *
     * @param detachable detachable to detach with the dialog
     * @param <D> detachable type
     * @return the same detachable
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
     * @return closed flag
     */
    public boolean isClosed() {
        return closed || resources.isClosed();
    }

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
