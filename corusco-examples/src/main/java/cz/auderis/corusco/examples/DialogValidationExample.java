package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.dialog.FormDialog;
import cz.auderis.corusco.swing.dialog.FormDialogValidationBinding;
import cz.auderis.corusco.swing.dialog.ProblemFocusResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Demonstrates dialog validation summary and focus behavior.
 *
 * <p>The example attempts to commit a form with validation problems, reports
 * the summary, and resolves focus to the relevant component. It shows how model
 * problems, dialog commit policy, and Swing focus handling cooperate.</p>
 */
public final class DialogValidationExample {

    private static final ComponentKey<FocusPanel> NAME_COMPONENT =
            ComponentKey.of("customer/name-field", FocusPanel.class);
    private static final ProblemCode REQUIRED = ProblemCode.of("validation/required");

    private DialogValidationExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a validation summary scenario.
     *
     * @return diagnostics describing summary and focus behavior
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            CustomerForm form = new CustomerForm();
            FormDialog<CustomerForm, String> dialog = new FormDialog<>(form, new JPanel());
            JLabel summary = new JLabel("ready");
            FocusPanel nameField = new FocusPanel();

            try (FormDialogValidationBinding binding = FormDialogValidationBinding.install(
                    dialog,
                    summary,
                    ProblemFocusResolver.componentTargets(Map.of(NAME_COMPONENT, nameField))
            )) {
                // Dialog problem summaries are refreshed explicitly because the
                // current FormModel contract exposes synchronous problems, not
                // an observable problem stream.
                form.problems = ProblemSet.of(Problem.validation(
                        REQUIRED,
                        ProblemSeverity.ERROR,
                        ProblemTarget.component(NAME_COMPONENT),
                        "Name required"
                ));
                binding.refresh();
                result.add("summary=" + summary.getText());

                // The resolver keeps focus ownership typed and explicit. A
                // generated dialog can map ComponentKey constants to accessors
                // without using reflection or property-path strings.
                result.add("focused=" + binding.focusFirstProblem());
                result.add("focusRequests=" + nameField.focusRequests);
            }
            result.add("restored=" + summary.getText());
        });
        return result;
    }

    private static final class CustomerForm implements FormModel<String> {

        private ProblemSet problems = ProblemSet.empty();

        @Override
        public ProblemSet problems() {
            return problems;
        }

        @Override
        public boolean isCommittable() {
            return !problems.hasErrors();
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

    private static final class FocusPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private int focusRequests;

        @Override
        public boolean requestFocusInWindow() {
            focusRequests++;
            return true;
        }
    }
}
