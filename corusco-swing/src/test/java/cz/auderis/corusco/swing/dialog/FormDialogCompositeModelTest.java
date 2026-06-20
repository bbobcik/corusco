package cz.auderis.corusco.swing.dialog;

import cz.auderis.corusco.core.form.AbstractCompositeFormModel;
import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormDialogCompositeModelTest {

    private static final FieldKey<ChildForm, String> CHILD_FIELD =
            FieldKey.of("child/value", ChildForm.class, String.class);

    @Test
    void compositeChildErrorBlocksOkAndApply() {
        SwingEdt.runAndWait(() -> {
            ChildForm valid = new ChildForm("valid");
            ChildForm invalid = new ChildForm("invalid");
            invalid.problems = ProblemSet.of(problem("child-error"));
            CompositeForm composite = new CompositeForm(valid, invalid);
            FormDialog<CompositeForm, String> dialog = new FormDialog<>(composite, new JPanel());

            assertThat(dialog.okCommand().isEnabled()).isFalse();
            assertThat(dialog.applyCommand().isEnabled()).isFalse();
            assertThat(dialog.accept()).isFalse();
            assertThat(dialog.apply()).isFalse();

            assertThat(valid.toResultCalls).isZero();
            assertThat(invalid.toResultCalls).isZero();
            assertThat(dialog.result().isAccepted()).isFalse();
            assertThat(dialog.isClosed()).isFalse();
        });
    }

    @Test
    void compositeParentErrorBlocksOkAndApply() {
        SwingEdt.runAndWait(() -> {
            ChildForm first = new ChildForm("first");
            ChildForm second = new ChildForm("second");
            CompositeForm composite = new CompositeForm(first, second);
            composite.parentProblems = ProblemSet.of(problem("parent-error"));
            FormDialog<CompositeForm, String> dialog = new FormDialog<>(composite, new JPanel());

            assertThat(dialog.okCommand().isEnabled()).isFalse();
            assertThat(dialog.applyCommand().isEnabled()).isFalse();
            assertThat(dialog.accept()).isFalse();
            assertThat(dialog.apply()).isFalse();

            assertThat(first.toResultCalls).isZero();
            assertThat(second.toResultCalls).isZero();
            assertThat(dialog.result().isAccepted()).isFalse();
        });
    }

    @Test
    void compositeCancelResetsEveryChild() {
        SwingEdt.runAndWait(() -> {
            ChildForm first = new ChildForm("first");
            ChildForm second = new ChildForm("second");
            CompositeForm composite = new CompositeForm(first, second);
            FormDialog<CompositeForm, String> dialog = new FormDialog<>(composite, new JPanel());

            assertThat(dialog.cancel()).isTrue();

            assertThat(first.resetCalls).isEqualTo(1);
            assertThat(second.resetCalls).isEqualTo(1);
            assertThat(composite.parentResetCalls).isEqualTo(1);
            assertThat(dialog.result().isAccepted()).isFalse();
            assertThat(dialog.isClosed()).isTrue();
        });
    }

    @Test
    void compositeApplyCancelReturnsLastAppliedAggregateResult() {
        SwingEdt.runAndWait(() -> {
            ChildForm first = new ChildForm("first");
            ChildForm second = new ChildForm("second");
            CompositeForm composite = new CompositeForm(first, second);
            FormDialog<CompositeForm, String> dialog = new FormDialog<>(composite, new JPanel());

            assertThat(dialog.apply()).isTrue();
            first.result = "ignored";
            assertThat(dialog.cancel()).isTrue();

            assertThat(dialog.result().acceptedValue()).contains("first+second");
            assertThat(first.acceptCalls).isEqualTo(1);
            assertThat(second.acceptCalls).isEqualTo(1);
            assertThat(composite.parentAcceptCalls).isEqualTo(1);
            assertThat(first.resetCalls).isEqualTo(1);
            assertThat(second.resetCalls).isEqualTo(1);
        });
    }

    @Test
    void compositeValidationBindingSummarizesAndRevealsChildProblemTarget() {
        SwingEdt.runAndWait(() -> {
            ChildForm first = new ChildForm("first");
            ChildForm second = new ChildForm("second");
            first.problems = ProblemSet.of(Problem.validation(
                    ProblemCode.of("child-required"),
                    ProblemSeverity.ERROR,
                    ProblemTarget.field(CHILD_FIELD),
                    "Child value required"
            ));
            CompositeForm composite = new CompositeForm(first, second);
            FormDialog<CompositeForm, String> dialog = new FormDialog<>(composite, new JPanel());
            JLabel summary = new JLabel();
            FocusPanel childField = new FocusPanel();
            Counter revealChild = new Counter();
            FormDialogValidationBinding binding = FormDialogValidationBinding.install(
                    dialog,
                    summary,
                    ProblemFocusResolver.withPreparation(
                            revealChild::increment,
                            ProblemFocusResolver.fieldTargets(Map.of(CHILD_FIELD, childField))
                    )
            );

            assertThat(summary.getText()).isEqualTo("Child value required");
            assertThat(binding.focusFirstProblem()).isTrue();
            assertThat(revealChild.value()).isEqualTo(1);
            assertThat(childField.focusRequests).isEqualTo(1);
        });
    }

    private static Problem problem(String id) {
        return Problem.validation(
                ProblemCode.of(id),
                ProblemSeverity.ERROR,
                ProblemTarget.form(),
                id
        );
    }

    private static final class CompositeForm extends AbstractCompositeFormModel<String> {

        private final ChildForm first;
        private final ChildForm second;
        private ProblemSet parentProblems = ProblemSet.empty();
        private int parentResetCalls;
        private int parentAcceptCalls;

        private CompositeForm(ChildForm first, ChildForm second) {
            super(first, second);
            this.first = first;
            this.second = second;
        }

        @Override
        protected String createResult() {
            return first.toResult() + "+" + second.toResult();
        }

        @Override
        protected ProblemSet validationProblems() {
            return parentProblems;
        }

        @Override
        protected void resetParentState() {
            parentResetCalls++;
        }

        @Override
        protected void acceptParentCurrentValues() {
            parentAcceptCalls++;
        }
    }

    private static final class ChildForm implements FormModel<String> {

        private String result;
        private ProblemSet problems = ProblemSet.empty();
        private int toResultCalls;
        private int resetCalls;
        private int acceptCalls;

        private ChildForm(String result) {
            this.result = result;
        }

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
            resetCalls++;
        }

        @Override
        public void acceptCurrentValues() {
            acceptCalls++;
        }

        @Override
        public String toResult() {
            toResultCalls++;
            return result;
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
