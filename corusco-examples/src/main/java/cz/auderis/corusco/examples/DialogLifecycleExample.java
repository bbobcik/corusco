package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.swing.binding.Binding;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.dialog.FormDialog;
import cz.auderis.corusco.swing.dialog.FormDialogLifecycle;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Demonstrates dialog lifecycle ownership.
 *
 * <p>The scenario registers bindings, task services, detachables, and other
 * cleanup actions with a dialog lifecycle and then closes them as one unit. It
 * is the example counterpart to the dialog lifecycle API documentation.</p>
 */
public final class DialogLifecycleExample {

    private DialogLifecycleExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a dialog lifecycle cleanup scenario.
     *
     * @return diagnostics describing cleanup order and closure
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            FormDialog<CustomerForm, String> dialog = new FormDialog<>(new CustomerForm(), new JPanel());
            FormDialogLifecycle lifecycle = FormDialogLifecycle.of(dialog);

            // The lifecycle is the single owner for dialog-scoped bindings.
            // Registering them here keeps generated presenters from leaking
            // listeners when a modal dialog is opened repeatedly.
            lifecycle.addBinding(tracked("binding", result));
            lifecycle.addDetachable(() -> result.add("detached"));

            // Closing the lifecycle releases owned resources first, then closes
            // the dialog controller. Late registrations fail closed immediately.
            lifecycle.close();
            lifecycle.addBinding(tracked("late", result));
            result.add("dialogClosed=" + dialog.isClosed());
        });
        return result;
    }

    private static Binding tracked(String name, List<String> result) {
        return () -> result.add(name);
    }

    private static final class CustomerForm implements FormModel<String> {

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
