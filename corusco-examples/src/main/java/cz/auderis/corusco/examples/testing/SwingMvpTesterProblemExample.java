package cz.auderis.corusco.examples.testing;

import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.swing.testing.SwingComponentKeys;
import cz.auderis.corusco.swing.testing.SwingMvpTester;

import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Demonstrates tester problem assertions.
 *
 * <p>The scenario uses tester helpers to assert validation problems and their
 * targets. It is focused on test readability for forms that expose Corusco
 * problem sets.</p>
 */
public final class SwingMvpTesterProblemExample {

    private static final ComponentKey<JTextField> NAME_COMPONENT =
            ComponentKey.of("customer/name-field", JTextField.class);
    private static final FieldKey<CustomerEdit, String> NAME =
            FieldKey.of("customer/name", CustomerEdit.class, String.class);
    private static final ProblemCode REQUIRED = ProblemCode.of("validation/required");

    private SwingMvpTesterProblemExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a validation assertion scenario.
     *
     * @return diagnostics from the invalid and valid passes
     */
    public static List<String> runScenario() {
        SwingMvpTester<CustomerView, CustomerPresenter> tester = SwingMvpTester.create(
                CustomerView::new,
                CustomerPresenter::new
        );

        // The test drives Swing through a generated-style ComponentKey, then
        // lets the presenter derive typed problems from the current view state.
        tester.enterText(NAME_COMPONENT, "")
                .runOnEdt((view, presenter) -> presenter.validate(view.nameField.getText()))
                .assertProblem((view, presenter) -> presenter.problems(), NAME, REQUIRED);
        String invalidCount = tester.queryOnEdt((view, presenter) ->
                Integer.toString(presenter.problems().size()));

        // Once valid input is present, the same typed FieldKey/ProblemCode pair
        // should be absent; no test code has to search messages or field names.
        tester.enterText(NAME_COMPONENT, "Alice")
                .runOnEdt((view, presenter) -> presenter.validate(view.nameField.getText()))
                .assertNoProblem((view, presenter) -> presenter.problems(), NAME, REQUIRED);

        return tester.queryOnEdt((view, presenter) -> List.of(
                invalidCount,
                Integer.toString(presenter.problems().size()),
                view.nameField.getText()
        ));
    }

    private record CustomerEdit(String name) {
    }

    private static final class CustomerView extends JPanel {

        private static final long serialVersionUID = 1L;

        private final JTextField nameField = SwingComponentKeys.mark(new JTextField(), NAME_COMPONENT);

        private CustomerView() {
            add(nameField);
        }
    }

    private static final class CustomerPresenter {

        private ProblemSet problems = ProblemSet.empty();

        private CustomerPresenter(CustomerView view) {
        }

        private void validate(String name) {
            problems = name.isBlank()
                    ? ProblemSet.of(Problem.validation(
                            REQUIRED,
                            ProblemSeverity.ERROR,
                            ProblemTarget.field(NAME),
                            "Name is required"
                    ))
                    : ProblemSet.empty();
        }

        private ProblemSet problems() {
            return problems;
        }
    }
}
