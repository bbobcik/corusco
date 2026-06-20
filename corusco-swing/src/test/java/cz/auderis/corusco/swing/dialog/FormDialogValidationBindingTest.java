package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.swing.binding.SwingEdt;

import java.util.Map;
import java.util.Optional;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormDialogValidationBindingTest {

    private static final ProblemCode REQUIRED = ProblemCode.of("validation/required");
    private static final ProblemCode WARNING = ProblemCode.of("validation/warning");
    private static final ComponentKey<FocusPanel> NAME_COMPONENT =
            ComponentKey.of("customer/name-field", FocusPanel.class);
    private static final ComponentKey<FocusPanel> CREDIT_COMPONENT =
            ComponentKey.of("customer/credit-field", FocusPanel.class);
    private static final FieldKey<TestForm, String> NAME_FIELD =
            FieldKey.of("customer/name", TestForm.class, String.class);
    private static final FieldKey<TestForm, Integer> AGE_FIELD =
            FieldKey.of("customer/age", TestForm.class, Integer.class);

    @Test
    void refreshClearsSummaryWhenThereAreNoProblems() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm();
            JLabel summary = new JLabel("previous");
            FormDialogValidationBinding binding = FormDialogValidationBinding.install(dialog(form), summary);

            assertThat(summary.getText()).isEmpty();
            binding.close();
        });
    }

    @Test
    void refreshShowsProblemCountAndHighestSeverityMessage() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm();
            form.problems = ProblemSet.of(
                    problem(WARNING, ProblemSeverity.WARNING, ProblemTarget.form(), "Looks unusual"),
                    problem(REQUIRED, ProblemSeverity.ERROR, ProblemTarget.form(), "Name required")
            );
            JLabel summary = new JLabel();
            FormDialogValidationBinding binding = FormDialogValidationBinding.install(dialog(form), summary);

            assertThat(summary.getText()).isEqualTo("2 problems: Name required");

            form.problems = ProblemSet.of(problem(WARNING, ProblemSeverity.WARNING, ProblemTarget.form(), "Only warning"));
            binding.refresh();

            assertThat(summary.getText()).isEqualTo("Only warning");
        });
    }

    @Test
    void focusFirstProblemUsesSeverityOrderedResolver() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm();
            Problem warning = problem(WARNING, ProblemSeverity.WARNING, ProblemTarget.form(), "Warning");
            Problem error = problem(REQUIRED, ProblemSeverity.ERROR, ProblemTarget.form(), "Error");
            FocusPanel errorComponent = new FocusPanel();
            form.problems = ProblemSet.of(warning, error);
            FormDialogValidationBinding binding = FormDialogValidationBinding.install(
                    dialog(form),
                    new JLabel(),
                    problem -> problem == error ? Optional.of(errorComponent) : Optional.empty()
            );

            assertThat(binding.focusFirstProblem()).isTrue();
            assertThat(errorComponent.focusRequests).isEqualTo(1);
        });
    }

    @Test
    void componentTargetResolverUsesTypedComponentKeys() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm();
            FocusPanel credit = new FocusPanel();
            FocusPanel name = new FocusPanel();
            form.problems = ProblemSet.of(problem(
                    REQUIRED,
                    ProblemSeverity.ERROR,
                    ProblemTarget.component(NAME_COMPONENT),
                    "Name required"
            ));
            FormDialogValidationBinding binding = FormDialogValidationBinding.install(
                    dialog(form),
                    new JLabel(),
                    ProblemFocusResolver.componentTargets(Map.of(CREDIT_COMPONENT, credit, NAME_COMPONENT, name))
            );

            assertThat(binding.focusFirstProblem()).isTrue();
            assertThat(name.focusRequests).isEqualTo(1);
            assertThat(credit.focusRequests).isZero();
        });
    }

    @Test
    void fieldTargetResolverUsesTypedFieldKeys() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm();
            FocusPanel name = new FocusPanel();
            FocusPanel age = new FocusPanel();
            form.problems = ProblemSet.of(problem(
                    REQUIRED,
                    ProblemSeverity.ERROR,
                    ProblemTarget.field(NAME_FIELD),
                    "Name required"
            ));
            FormDialogValidationBinding binding = FormDialogValidationBinding.install(
                    dialog(form),
                    new JLabel(),
                    ProblemFocusResolver.fieldTargets(Map.of(AGE_FIELD, age, NAME_FIELD, name))
            );

            assertThat(binding.focusFirstProblem()).isTrue();
            assertThat(name.focusRequests).isEqualTo(1);
            assertThat(age.focusRequests).isZero();
        });
    }

    @Test
    void resolverCompositionCanRevealOwningChildBeforeFocus() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm();
            FocusPanel profileField = new FocusPanel();
            FocusPanel securityField = new FocusPanel();
            Counter profileReveal = new Counter();
            Counter securityReveal = new Counter();
            form.problems = ProblemSet.of(problem(
                    REQUIRED,
                    ProblemSeverity.ERROR,
                    ProblemTarget.field(AGE_FIELD),
                    "Age required"
            ));
            ProblemFocusResolver resolver = ProblemFocusResolver.firstOf(
                    ProblemFocusResolver.withPreparation(
                            profileReveal::increment,
                            ProblemFocusResolver.fieldTargets(Map.of(NAME_FIELD, profileField))
                    ),
                    ProblemFocusResolver.withPreparation(
                            securityReveal::increment,
                            ProblemFocusResolver.fieldTargets(Map.of(AGE_FIELD, securityField))
                    )
            );
            FormDialogValidationBinding binding = FormDialogValidationBinding.install(dialog(form), new JLabel(), resolver);

            assertThat(binding.focusFirstProblem()).isTrue();
            assertThat(profileReveal.value()).isZero();
            assertThat(securityReveal.value()).isEqualTo(1);
            assertThat(profileField.focusRequests).isZero();
            assertThat(securityField.focusRequests).isEqualTo(1);
        });
    }

    @Test
    void missingFocusTargetIsIgnored() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm();
            form.problems = ProblemSet.of(problem(REQUIRED, ProblemSeverity.ERROR, ProblemTarget.form(), "Form error"));
            FormDialogValidationBinding binding = FormDialogValidationBinding.install(dialog(form), new JLabel());

            assertThat(binding.focusFirstProblem()).isFalse();
        });
    }

    @Test
    void closeRestoresPreviousSummaryTextAndSuppressesLaterRefresh() {
        SwingEdt.runAndWait(() -> {
            TestForm form = new TestForm();
            JLabel summary = new JLabel("previous");
            FormDialogValidationBinding binding = FormDialogValidationBinding.install(dialog(form), summary);

            binding.close();
            form.problems = ProblemSet.of(problem(REQUIRED, ProblemSeverity.ERROR, ProblemTarget.form(), "Late"));
            binding.refresh();

            assertThat(summary.getText()).isEqualTo("previous");
            assertThat(binding.focusFirstProblem()).isFalse();
        });
    }

    private static FormDialog<TestForm, String> dialog(TestForm form) {
        return new FormDialog<>(form, new JPanel());
    }

    private static Problem problem(
            ProblemCode code,
            ProblemSeverity severity,
            ProblemTarget target,
            String message
    ) {
        return Problem.validation(code, severity, target, message);
    }

    private static final class TestForm implements FormModel<String> {

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
            return "ok";
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

    private static final class Counter {

        private int value;

        private void increment() {
            value++;
        }

        private int value() {
            return value;
        }
    }
}
