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
 * Installs root-pane keyboard behavior for a {@link FormDialog}.
 *
 * <p>This binding owns the Swing keyboard wiring that is deliberately kept out
 * of {@code FormDialog} itself. It maps Escape in the root pane's
 * {@link JComponent#WHEN_IN_FOCUSED_WINDOW} input map to the dialog's cancel
 * command and can install a default OK button. The dialog remains responsible
 * for cancel semantics; this class only routes keyboard events and default
 * button state.</p>
 *
 * <p>Instances are mutable lifecycle handles, Event Dispatch Thread confined,
 * and one-shot for one installed root pane. The binding records the previous
 * Escape input-map value, previous action-map entry, and previous default
 * button, then restores those values on {@link #close()}. Closing is idempotent
 * and does not close the dialog controller.</p>
 *
 * <p>Use this class when a modal shell or internal frame should share the
 * standard dialog keyboard policy. Avoid installing multiple keyboard bindings
 * for the same root pane unless a higher-level lifecycle clearly owns their
 * ordering.</p>
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
     * Installs Escape handling on a root pane.
     *
     * @param rootPane root pane whose input and action maps will be modified,
     *         not {@code null}
     * @param dialog dialog controller whose cancel command will be invoked, not
     *         {@code null}
     * @return installed binding; close it to restore previous root-pane state
     * @throws IllegalStateException if called off the EDT
     */
    public static FormDialogKeyboardBinding install(JRootPane rootPane, FormDialog<?, ?> dialog) {
        return install(rootPane, dialog, null);
    }

    /**
     * Installs Escape handling and optionally a default button on a root pane.
     *
     * <p>If {@code defaultButton} is {@code null}, the current default button is
     * left unchanged during installation and remains the value restored on
     * close.</p>
     *
     * @param rootPane root pane whose input/action maps will be modified, not
     *         {@code null}
     * @param dialog dialog controller whose cancel command will be invoked, not
     *         {@code null}
     * @param defaultButton default button to install, or {@code null} to leave
     *         the current button unchanged
     * @return installed binding; close it to restore previous root-pane state
     * @throws IllegalStateException if called off the EDT
     */
    public static FormDialogKeyboardBinding install(
            JRootPane rootPane,
            FormDialog<?, ?> dialog,
            JButton defaultButton
    ) {
        return new FormDialogKeyboardBinding(rootPane, dialog, defaultButton);
    }

    /**
     * Restores the root pane's previous Escape mapping and default button.
     *
     * @throws IllegalStateException if called off the EDT
     */
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
