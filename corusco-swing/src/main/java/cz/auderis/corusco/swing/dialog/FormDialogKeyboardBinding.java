package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/**
 * Installs root-pane keyboard behavior for a form dialog.
 *
 * <p>The binding maps ESC to the dialog's existing cancel command and can
 * assign an OK/default button. Closing restores the previous ESC mapping,
 * previous action, and previous default button so modal shells can be reused in
 * tests and long-lived views.</p>
 */
public final class FormDialogKeyboardBinding implements Binding {

    private static final KeyStroke ESCAPE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    private static final String ACTION_KEY = "corusco.dialog.cancel";

    private final JRootPane rootPane;
    private final Object previousInput;
    private final Action previousAction;
    private final JButton previousDefaultButton;
    private boolean closed;

    private FormDialogKeyboardBinding(JRootPane rootPane, FormDialog<?, ?> dialog, JButton defaultButton) {
        SwingEdt.requireEdt();
        this.rootPane = Objects.requireNonNull(rootPane, "rootPane");
        Objects.requireNonNull(dialog, "dialog");
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.previousInput = inputMap.get(ESCAPE);
        this.previousAction = rootPane.getActionMap().get(ACTION_KEY);
        this.previousDefaultButton = rootPane.getDefaultButton();

        inputMap.put(ESCAPE, ACTION_KEY);
        rootPane.getActionMap().put(ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dialog.cancelCommand().execute();
            }
        });
        if (defaultButton != null) {
            rootPane.setDefaultButton(defaultButton);
        }
    }

    /**
     * Installs ESC handling on a root pane.
     *
     * @param rootPane root pane
     * @param dialog dialog controller
     * @return installed binding
     */
    public static FormDialogKeyboardBinding install(JRootPane rootPane, FormDialog<?, ?> dialog) {
        return install(rootPane, dialog, null);
    }

    /**
     * Installs ESC handling and a default button on a root pane.
     *
     * @param rootPane root pane
     * @param dialog dialog controller
     * @param defaultButton default button, or {@code null} to leave unchanged
     * @return installed binding
     */
    public static FormDialogKeyboardBinding install(
            JRootPane rootPane,
            FormDialog<?, ?> dialog,
            JButton defaultButton
    ) {
        return new FormDialogKeyboardBinding(rootPane, dialog, defaultButton);
    }

    @Override
    public void close() {
        SwingEdt.requireEdt();
        if (closed) {
            return;
        }
        closed = true;
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        if (previousInput == null) {
            inputMap.remove(ESCAPE);
        } else {
            inputMap.put(ESCAPE, previousInput);
        }
        if (previousAction == null) {
            rootPane.getActionMap().remove(ACTION_KEY);
        } else {
            rootPane.getActionMap().put(ACTION_KEY, previousAction);
        }
        rootPane.setDefaultButton(previousDefaultButton);
    }
}
