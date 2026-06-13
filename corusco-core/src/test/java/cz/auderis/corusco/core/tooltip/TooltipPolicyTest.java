package cz.auderis.corusco.core.tooltip;

import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TooltipPolicyTest {

    private static final ProblemCode REQUIRED = ProblemCode.of("required");
    private static final ProblemCode RANGE = ProblemCode.of("range");

    @Test
    void composesTooltipPartsInRoadmapOrder() {
        ProblemSet problems = ProblemSet.of(
                problem(RANGE, ProblemSeverity.WARNING, "Credit limit should be reviewed"),
                problem(REQUIRED, ProblemSeverity.ERROR, "Name is required")
        );
        TooltipContent content = new TooltipContent(
                problems,
                "Save is disabled until errors are fixed",
                "Enter the customer display name",
                true
        );

        assertThat(TooltipPolicy.standard().composeLines(content)).containsExactly(
                "Name is required",
                "Save is disabled until errors are fixed",
                "Enter the customer display name",
                TooltipPolicy.DEFAULT_HELP_INDICATOR
        );
    }

    @Test
    void preservesProblemInsertionOrderWhenSeverityTies() {
        ProblemSet problems = ProblemSet.of(
                problem(REQUIRED, ProblemSeverity.ERROR, "First error"),
                problem(RANGE, ProblemSeverity.ERROR, "Second error")
        );

        assertThat(TooltipPolicy.standard().composeLines(new TooltipContent(problems, "", "", false)))
                .containsExactly("First error");
    }

    @Test
    void returnsEmptyWhenNoTooltipPartsArePresent() {
        assertThat(TooltipPolicy.standard().compose(TooltipContent.empty())).isEmpty();
    }

    @Test
    void ignoresBlankTextPartsAndSupportsCustomHelpIndicator() {
        TooltipPolicy policy = new TooltipPolicy("Open contextual help");
        TooltipContent content = new TooltipContent(null, "  ", "\nStatic help\n", true);

        assertThat(policy.composeLines(content)).containsExactly(
                "Static help",
                "Open contextual help"
        );
    }

    @Test
    void canSuppressHelpIndicator() {
        TooltipContent content = TooltipContent.help("Static help", true);

        assertThat(TooltipPolicy.withoutHelpIndicator().composeLines(content))
                .containsExactly("Static help");
    }

    @Test
    void rejectsNullContent() {
        assertThatThrownBy(() -> TooltipPolicy.standard().composeLines(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("content");
    }

    private static Problem problem(ProblemCode code, ProblemSeverity severity, String message) {
        return Problem.validation(code, severity, ProblemTarget.form(), message);
    }
}
