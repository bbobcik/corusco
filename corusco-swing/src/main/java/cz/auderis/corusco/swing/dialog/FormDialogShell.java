package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.dialog.DialogResult;
import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

/**
 * Minimal native Swing shell for a {@link FormDialog} controller.
 *
 * <p>{@code FormDialogShell} creates and owns a modal {@link JDialog}, hosts
 * the controller's existing root component, and delegates user-level OK, Apply,
 * Revert, and Cancel operations to the controller. It does not build form layouts,
 * validation summaries, button bars, resource lookups, or confirmation UI. Keep
 * those application-owned and connect buttons to {@link #accept()},
 * {@link #apply()}, and {@link #cancel()} or to custom actions that call those
 * methods.</p>
 *
 * <p>Native window closing uses {@link FormDialog#cancel()}, so dirty-state and
 * {@link CancelConfirmation} policies remain exactly the same as for an
 * application cancel button. Programmatic {@link #close()} is lifecycle cleanup
 * and delegates to {@link FormDialog#close()}, which intentionally bypasses
 * dirty-cancel confirmation.</p>
 *
 * <p>Instances are Event Dispatch Thread confined. Construct, show, operate,
 * and close them on the EDT.</p>
 *
 * @param <P> form model type
 * @param <R> committed result type
 */
public final class FormDialogShell<P extends FormModel<R>, R> implements Binding {

    private final FormDialog<P, R> controller;
    private final JDialog dialog;

    /**
     * Creates a modal dialog shell.
     *
     * @param owner native owner window; may be {@code null}
     * @param title native dialog title
     * @param controller form dialog controller
     */
    public FormDialogShell(Window owner, String title, FormDialog<P, R> controller) {
        SwingEdt.requireEdt();
        this.controller = Objects.requireNonNull(controller, "controller");
        this.dialog = new JDialog(owner, Objects.requireNonNull(title, "title"), Dialog.ModalityType.APPLICATION_MODAL);
        this.dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.dialog.setContentPane(controller.root());
        this.dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                cancel();
            }
        });
        this.dialog.pack();
        this.dialog.setLocationRelativeTo(owner);
    }

    /**
     * Creates a modal dialog shell.
     *
     * @param owner native owner window; may be {@code null}
     * @param title native dialog title
     * @param controller form dialog controller
     * @param <P> form model type
     * @param <R> committed result type
     * @return dialog shell
     */
    public static <P extends FormModel<R>, R> FormDialogShell<P, R> create(
            Window owner,
            String title,
            FormDialog<P, R> controller
    ) {
        return new FormDialogShell<>(owner, title, controller);
    }

    /**
     * Returns the form dialog controller.
     *
     * @return controller
     */
    public FormDialog<P, R> controller() {
        return controller;
    }

    /**
     * Returns the native dialog.
     *
     * @return dialog
     */
    public JDialog dialog() {
        return dialog;
    }

    /**
     * Shows the modal shell and returns the controller result after it closes.
     *
     * @return dialog result
     */
    public DialogResult<R> showModal() {
        SwingEdt.requireEdt();
        dialog.setVisible(true);
        return controller.result();
    }

    /**
     * Runs OK semantics and disposes the shell after successful acceptance.
     *
     * @return {@code true} when the controller accepted and closed
     */
    public boolean accept() {
        SwingEdt.requireEdt();
        boolean accepted = controller.accept();
        if (accepted) {
            dialog.dispose();
        }
        return accepted;
    }

    /**
     * Runs Apply semantics without disposing the shell.
     *
     * @return {@code true} when the controller applied successfully
     */
    public boolean apply() {
        SwingEdt.requireEdt();
        return controller.apply();
    }

    /**
     * Runs Revert semantics and disposes the shell after successful restore.
     *
     * @return {@code true} when the controller reverted and closed
     */
    public boolean revert() {
        SwingEdt.requireEdt();
        boolean reverted = controller.revert();
        if (reverted) {
            dialog.dispose();
        }
        return reverted;
    }

    /**
     * Runs user cancellation and disposes the shell when cancellation succeeds.
     *
     * @return {@code true} when cancellation closed the controller or it was
     *         already closed
     */
    public boolean cancel() {
        SwingEdt.requireEdt();
        boolean cancelled = controller.cancel();
        if (cancelled) {
            dialog.dispose();
        }
        return cancelled;
    }

    /**
     * Forces lifecycle cleanup and disposes the native shell.
     */
    @Override
    public void close() {
        SwingEdt.requireEdt();
        controller.close();
        dialog.dispose();
    }
}
