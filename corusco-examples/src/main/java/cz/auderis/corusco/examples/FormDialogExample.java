package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.dialog.FormDialog;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Demonstrates base form dialog controller semantics.
 */
public final class FormDialogExample {

    private FormDialogExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs OK, Apply, and Cancel dialog flows.
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

        private final String name;

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
