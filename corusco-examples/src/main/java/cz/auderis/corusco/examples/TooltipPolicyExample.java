package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.core.tooltip.TooltipContent;
import cz.auderis.corusco.core.tooltip.TooltipPolicy;

import java.util.List;
import java.util.Map;

/**
 * Demonstrates toolkit-neutral tooltip composition.
 *
 * <p>The example combines validation feedback, disabled reasons, static help,
 * and help availability through a core tooltip policy. It shows the text
 * composition rules before Swing bindings write the result to a component.</p>
 */
public final class TooltipPolicyExample {

    private static final ProblemCode REQUIRED = ProblemCode.of("required");

    private TooltipPolicyExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Composes generated resource help with dynamic field feedback.
     *
     * @return ordered tooltip lines
     */
    public static List<String> runScenario() {
        Resources resources = Resources.of(Map.of(
                GeneratedCustomerRowTableResources.NAME_TOOLTIP.id(), "Customer display name"
        ));
        ProblemSet problems = ProblemSet.of(Problem.validation(
                REQUIRED,
                ProblemSeverity.ERROR,
                ProblemTarget.form(),
                "Customer name is required"
        ));

        // Generated resource keys provide the static part; validation supplies
        // the dynamic part that should be shown first.
        String staticHelp = resources.resolve(
                GeneratedCustomerRowTableResources.NAME_TOOLTIP,
                ""
        );

        // The descriptor's help topic controls whether the common F1 hint is
        // present. Swing code later decides how these lines become a tooltip.
        TooltipContent content = new TooltipContent(
                problems,
                "Save is disabled until the required fields are valid",
                staticHelp,
                GeneratedCustomerRowColumns.NAME_DESCRIPTOR.helpTopic() != null
        );

        return TooltipPolicy.standard().composeLines(content);
    }
}
