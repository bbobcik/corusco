package cz.auderis.corusco.core.tooltip;

import cz.auderis.corusco.core.problem.ProblemSet;

import java.util.Optional;

/**
 * Toolkit-neutral inputs for tooltip composition.
 *
 * <p>The record intentionally keeps dynamic validation feedback, disabled
 * state, static help text, and help availability separate. Swing bindings can
 * update those parts independently and let {@link TooltipPolicy} decide the
 * presentation order.</p>
 *
 * @param problems validation, parse, or feedback problems relevant to the
 *         target being described
 * @param disabledReason reason why the target is disabled, or blank when not
 *         applicable
 * @param staticHelp static help text from descriptors or resources, or blank
 *         when absent
 * @param helpAvailable whether F1/context help can be opened for this target
 */
public record TooltipContent(
        ProblemSet problems,
        String disabledReason,
        String staticHelp,
        boolean helpAvailable
) {

    /**
     * Creates tooltip content.
     *
     * <p>A {@code null} problem set is treated as empty. {@code null} or blank
     * text parts are normalized to blank strings so callers can pass optional
     * resource lookups directly without leaking {@code null} into composed
     * output.</p>
     *
     * @param problems problem set, or {@code null} for no problems
     * @param disabledReason disabled reason, or {@code null}/blank
     * @param staticHelp static help text, or {@code null}/blank
     * @param helpAvailable whether help is available
     */
    public TooltipContent {
        problems = (problems != null) ? problems : ProblemSet.empty();
        disabledReason = normalize(disabledReason);
        staticHelp = normalize(staticHelp);
    }

    /**
     * Creates empty tooltip content.
     *
     * @return empty content
     */
    public static TooltipContent empty() {
        return new TooltipContent(ProblemSet.empty(), "", "", false);
    }

    /**
     * Creates content for static descriptor/resource help.
     *
     * @param staticHelp static help text
     * @param helpAvailable whether F1/context help is available
     * @return tooltip content with no dynamic problems or disabled reason
     */
    public static TooltipContent help(String staticHelp, boolean helpAvailable) {
        return new TooltipContent(ProblemSet.empty(), "", staticHelp, helpAvailable);
    }

    /**
     * Returns the disabled reason when present.
     *
     * @return disabled reason, or empty
     */
    public Optional<String> disabledReasonOptional() {
        return optionalText(disabledReason);
    }

    /**
     * Returns the static help text when present.
     *
     * @return static help text, or empty
     */
    public Optional<String> staticHelpOptional() {
        return optionalText(staticHelp);
    }

    private static Optional<String> optionalText(String text) {
        return text.isBlank() ? Optional.empty() : Optional.of(text);
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String stripped = text.strip();
        return stripped.isBlank() ? "" : stripped;
    }
}
