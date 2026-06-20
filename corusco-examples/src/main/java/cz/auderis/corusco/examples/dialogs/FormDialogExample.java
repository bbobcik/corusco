package cz.auderis.corusco.examples.dialogs;

import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.dialog.FormDialog;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Demonstrates base form dialog controller semantics.
 *
 * <p>The example drives commit and cancel paths against a form dialog without a
 * visible window. It is the smallest scenario for understanding how a
 * UI-independent form model becomes a dialog result.</p>
 */
public final class FormDialogExample {

    private FormDialogExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs OK, Apply, Apply-Cancel, and Cancel dialog flows.
     *
     * @return diagnostics describing dialog results
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            CustomerForm form = new CustomerForm("Alice");
            FormDialog<CustomerForm, Customer> dialog = new FormDialog<>(form, new JPanel());

            // Apply validates and creates the domain value, but keeps the
            // controller open so the user can continue editing in the same
            // modal shell.
            dialog.applyCommand().execute();
            result.add("applied=" + dialog.lastAppliedResult().orElseThrow().name());
            result.add("closedAfterApply=" + dialog.isClosed());

            // OK follows the same commit path, then stores the terminal
            // accepted result and disables all dialog commands.
            dialog.okCommand().execute();
            result.add("accepted=" + dialog.result().acceptedValue().orElseThrow().name());
            result.add("closedAfterOk=" + dialog.isClosed());

            CustomerForm applyCancelForm = new CustomerForm("Carol");
            FormDialog<CustomerForm, Customer> applyCancelDialog =
                    new FormDialog<>(applyCancelForm, new JPanel());
            applyCancelDialog.apply();
            applyCancelForm.name = "discarded";
            applyCancelDialog.cancel();
            result.add("applyCancelAccepted=" + applyCancelDialog.result().isAccepted());
            result.add("applyCancelValue=" + applyCancelDialog.result().acceptedValue().orElseThrow().name());

            CustomerForm cancelForm = new CustomerForm("Bob");
            FormDialog<CustomerForm, Customer> cancelDialog = new FormDialog<>(cancelForm, new JPanel());
            cancelDialog.cancelCommand().execute();
            result.add("cancelledHasValue=" + cancelDialog.result().acceptedValue().isPresent());
        });
        return result;
    }

    private record Customer(String name) {
    }

    private static final class CustomerForm implements FormModel<Customer> {

        private String name;

        private CustomerForm(String name) {
            this.name = name;
        }

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
        public Customer toResult() {
            return new Customer(name);
        }
    }
}
