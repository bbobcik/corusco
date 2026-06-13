package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.dialog.FormDialog;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Demonstrates dirty-cancel confirmation for form dialogs.
 */
public final class DirtyCancelDialogExample {

    private DirtyCancelDialogExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs clean, rejected dirty, and confirmed dirty cancellation.
     *
     * @return diagnostics describing cancellation decisions
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            CustomerForm cleanForm = new CustomerForm();
            FormDialog<CustomerForm, String> cleanDialog = new FormDialog<>(
                    cleanForm,
                    new JPanel(),
                    cleanForm::isDirty,
                    () -> {
                        result.add("unexpected-confirmation");
                        return true;
                    }
            );

            // Clean forms cancel immediately. The confirmation hook is not
            // called, but reset still returns the form to its original baseline
            // before the modal shell is considered closed.
            cleanDialog.cancel();
            result.add("cleanClosed=" + cleanDialog.isClosed());
            result.add("cleanReset=" + cleanForm.resetCalls);

            CustomerForm dirtyForm = new CustomerForm();
            dirtyForm.dirty = true;
            FormDialog<CustomerForm, String> rejectedDialog = new FormDialog<>(
                    dirtyForm,
                    new JPanel(),
                    dirtyForm::isDirty,
                    () -> false
            );

            // A rejected dirty-cancel confirmation leaves the controller open
            // and does not discard edits. Applications can keep focus in the
            // dialog and let the user continue editing.
            result.add("dirtyRejected=" + rejectedDialog.cancel());
            result.add("dirtyStillOpen=" + !rejectedDialog.isClosed());
            result.add("dirtyReset=" + dirtyForm.resetCalls);

            FormDialog<CustomerForm, String> confirmedDialog = new FormDialog<>(
                    dirtyForm,
                    new JPanel(),
                    dirtyForm::isDirty,
                    () -> true
            );
            result.add("dirtyConfirmed=" + confirmedDialog.cancel());
            result.add("confirmedReset=" + dirtyForm.resetCalls);
        });
        return result;
    }

    private static final class CustomerForm implements FormModel<String> {

        private boolean dirty;
        private int resetCalls;

        private boolean isDirty() {
            return dirty;
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
            dirty = false;
            resetCalls++;
        }

        @Override
        public void acceptCurrentValues() {
            dirty = false;
        }

        @Override
        public String toResult() {
            return "customer";
        }
    }
}
