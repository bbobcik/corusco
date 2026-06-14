package cz.auderis.corusco.examples.dialogs;

import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.dialog.FormDialog;
import cz.auderis.corusco.swing.dialog.FormDialogKeyboardBinding;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/**
 * Demonstrates dialog keyboard and default-button wiring.
 *
 * <p>The example shows how a form dialog reacts to Enter, Escape, and default
 * button configuration without requiring a visible native window. It is focused
 * on controller semantics rather than visual layout.</p>
 */
public final class DialogKeyboardExample {

    private DialogKeyboardExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a keyboard binding scenario.
     *
     * @return diagnostics describing ESC and default-button behavior
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            CustomerForm form = new CustomerForm();
            FormDialog<CustomerForm, String> dialog = new FormDialog<>(form, new JPanel());
            JRootPane rootPane = new JRootPane();
            JButton okButton = new JButton("OK");

            try (FormDialogKeyboardBinding binding =
                         FormDialogKeyboardBinding.install(rootPane, dialog, okButton)) {
                // The native root pane owns default-button behavior. The
                // binding only chooses which button is the default so generated
                // dialogs can wire buttons without subclassing JDialog.
                result.add("defaultButton=" + rootPane.getDefaultButton().getText());

                // ESC delegates to the same cancel command as a Cancel button.
                // Dirty-cancel confirmation therefore remains centralized in
                // FormDialog instead of being duplicated in key handlers.
                triggerEscape(rootPane);
                result.add("closedByEscape=" + dialog.isClosed());
                result.add("resetCalls=" + form.resetCalls);
            }
            result.add("defaultRestored=" + (rootPane.getDefaultButton() == null));
        });
        return result;
    }

    private static void triggerEscape(JRootPane rootPane) {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        Object actionKey = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(escape);
        Action action = rootPane.getActionMap().get(actionKey);
        action.actionPerformed(new ActionEvent(rootPane, ActionEvent.ACTION_PERFORMED, "escape"));
    }

    private static final class CustomerForm implements FormModel<String> {

        private int resetCalls;

        @Override
        public ProblemSet problems() {
            return ProblemSet.empty();
        }

        @Override
        public boolean isCommittable() {
            return true;
        }

        @Override
        public void reset() {
            resetCalls++;
        }

        @Override
        public void acceptCurrentValues() {
        }

        @Override
        public String toResult() {
            return "customer";
        }
    }
}
