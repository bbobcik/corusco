package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemTarget;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbstractCompositeFormModelTest {

    @Test
    void childrenAreReturnedInRegistrationOrderAndCannotBeMutated() {
        ChildForm first = new ChildForm("first");
        ChildForm second = new ChildForm("second");
        CompositeForm composite = new CompositeForm(List.of(first, second));

        assertThat(composite.children()).hasSize(2);
        assertThat(composite.children().get(0)).isSameAs(first);
        assertThat(composite.children().get(1)).isSameAs(second);
        assertThatThrownBy(() -> composite.children().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void constructorRejectsNullAndDuplicateChildren() {
        ChildForm child = new ChildForm("child");

        assertThatNullPointerException()
                .isThrownBy(() -> new CompositeForm((List<FormModel<?>>) null))
                .withMessageContaining("children");
        assertThatNullPointerException()
                .isThrownBy(() -> new CompositeForm(Arrays.asList(child, null)))
                .withMessageContaining("child");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CompositeForm(List.of(child, child)))
                .withMessageContaining("Duplicate child form model");
    }

    @Test
    void problemsAggregateChildrenInOrderThenParentValidation() {
        Problem first = problem("first", ProblemSeverity.WARNING);
        Problem second = problem("second", ProblemSeverity.ERROR);
        Problem parent = problem("parent", ProblemSeverity.ERROR);
        CompositeForm composite = new CompositeForm(List.of(
                new ChildForm("first", first),
                new ChildForm("second", second)
        ));
        composite.parentProblems = ProblemSet.of(parent);

        assertThat(composite.problems().problems()).containsExactly(first, second, parent);
    }

    @Test
    void childOrParentErrorBlocksCommit() {
        CompositeForm childBlocked = new CompositeForm(List.of(
                new ChildForm("child", problem("child", ProblemSeverity.ERROR))
        ));
        CompositeForm parentBlocked = new CompositeForm(List.of(new ChildForm("child")));
        parentBlocked.parentProblems = ProblemSet.of(problem("parent", ProblemSeverity.ERROR));

        assertThat(childBlocked.isCommittable()).isFalse();
        assertThat(parentBlocked.isCommittable()).isFalse();
        assertThatThrownBy(childBlocked::toResult)
                .isInstanceOf(UncommittableFormException.class)
                .hasMessageContaining("Form is not committable");
        assertThatThrownBy(parentBlocked::toResult)
                .isInstanceOf(UncommittableFormException.class)
                .hasMessageContaining("Form is not committable");
    }

    @Test
    void resetAndBaselineAcceptanceDelegateToChildrenThenParent() {
        OperationLog log = new OperationLog();
        ChildForm first = new ChildForm("first", log);
        ChildForm second = new ChildForm("second", log);
        CompositeForm composite = new CompositeForm(List.of(first, second), log);

        composite.reset();
        composite.acceptCurrentValues();

        assertThat(log.operations).containsExactly(
                "reset:first",
                "reset:second",
                "reset:parent",
                "accept:first",
                "accept:second",
                "accept:parent"
        );
    }

    @Test
    void successfulCommitCreatesParentResult() {
        CompositeForm composite = new CompositeForm(List.of(
                new ChildForm("first"),
                new ChildForm("second")
        ));

        assertThat(composite.toResult()).isEqualTo("first+second");
    }

    private static Problem problem(String id, ProblemSeverity severity) {
        return Problem.validation(ProblemCode.of(id), severity, ProblemTarget.form(), id);
    }

    private static final class CompositeForm extends AbstractCompositeFormModel<String> {

        private ProblemSet parentProblems = ProblemSet.empty();
        private final OperationLog log;

        private CompositeForm(List<? extends FormModel<?>> children) {
            this(children, null);
        }

        private CompositeForm(List<? extends FormModel<?>> children, OperationLog log) {
            super(children);
            this.log = log;
        }

        @Override
        protected String createResult() {
            return children().stream()
                    .map(ChildForm.class::cast)
                    .map(child -> child.name)
                    .reduce((left, right) -> left + "+" + right)
                    .orElse("");
        }

        @Override
        protected ProblemSet validationProblems() {
            return parentProblems;
        }

        @Override
        protected void resetParentState() {
            if (log != null) {
                log.operations.add("reset:parent");
            }
        }

        @Override
        protected void acceptParentCurrentValues() {
            if (log != null) {
                log.operations.add("accept:parent");
            }
        }
    }

    private static final class ChildForm implements FormModel<String> {

        private final String name;
        private final ProblemSet problems;
        private final OperationLog log;

        private ChildForm(String name) {
            this(name, ProblemSet.empty(), null);
        }

        private ChildForm(String name, Problem problem) {
            this(name, ProblemSet.of(problem), null);
        }

        private ChildForm(String name, OperationLog log) {
            this(name, ProblemSet.empty(), log);
        }

        private ChildForm(String name, ProblemSet problems, OperationLog log) {
            this.name = name;
            this.problems = problems;
            this.log = log;
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
            if (log != null) {
                log.operations.add("reset:" + name);
            }
        }

        @Override
        public void acceptCurrentValues() {
            if (log != null) {
                log.operations.add("accept:" + name);
            }
        }

        @Override
        public String toResult() {
            return name;
        }
    }

    private static final class OperationLog {

        private final List<String> operations = new java.util.ArrayList<>();
    }
}
