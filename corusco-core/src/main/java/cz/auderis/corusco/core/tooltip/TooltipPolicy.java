package cz.auderis.corusco.core.tooltip;

import cz.auderis.corusco.core.problem.Problem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable policy for composing tooltip lines from validation, state, and help inputs.
 *
 * <p>Corusco gathers tooltip text from several places: parse and validation
 * problems, disabled command or field state, descriptor/resource help, and the
 * presence of an F1/context-help topic. Swing components have one tooltip slot,
 * so this policy defines a deterministic order for those inputs without making
 * the core module depend on Swing or HTML rendering.</p>
 *
 * <p>The policy returns ordered plain-text lines. Swing bindings can join those
 * lines into a component tooltip, while tests or accessibility adapters can
 * inspect the same ordered content directly. Instances are immutable and
 * reusable; {@link #standard()} is the normal generated-form/table policy,
 * placing the most severe problem first, then disabled reason, static help, and
 * finally the help indicator.</p>
 *
 * <p>The policy does not perform localization, resource lookup, or problem
 * filtering. Callers should pass already-localized static help and the problem
 * set relevant to the component or cell being rendered.</p>
 */
public final class TooltipPolicy {

    /**
     * Standard help indicator shown when a target has an F1/context help topic.
     */
    public static final String DEFAULT_HELP_INDICATOR = "Press F1 for help";

    private final String helpIndicator;

    /**
     * Creates a policy.
     *
     * <p>A {@code null} or blank help indicator suppresses the final F1 line
     * even when {@link TooltipContent#helpAvailable()} is {@code true}.</p>
     *
     * @param helpIndicator help indicator text, or blank to suppress it
     */
    public TooltipPolicy(String helpIndicator) {
        this.helpIndicator = normalize(helpIndicator);
    }

    /**
     * Creates the standard tooltip policy.
     *
     * <p>The standard policy appends {@link #DEFAULT_HELP_INDICATOR} when
     * help is available and a higher-priority tooltip part has not already
     * consumed the user's attention.</p>
     *
     * @return standard policy
     */
    public static TooltipPolicy standard() {
        return new TooltipPolicy(DEFAULT_HELP_INDICATOR);
    }

    /**
     * Creates a policy that does not add a help indicator line.
     *
     * @return policy without F1 indicator text
     */
    public static TooltipPolicy withoutHelpIndicator() {
        return new TooltipPolicy("");
    }

    /**
     * Composes tooltip content into ordered plain-text lines.
     *
     * <p>The order is fixed by this policy: the most severe validation or parse
     * problem first, then the disabled reason, static help, and finally the
     * F1/context help indicator.</p>
     *
     * @param content tooltip content
     * @return immutable ordered tooltip lines
     */
    public List<String> composeLines(TooltipContent content) {
        Objects.requireNonNull(content, "content");
        List<String> lines = new ArrayList<>(4);

        content.problems().bySeverityDescending().stream()
                .findFirst()
                .map(Problem::message)
                .flatMap(TooltipPolicy::optionalText)
                .ifPresent(lines::add);

        content.disabledReasonOptional().ifPresent(lines::add);
        content.staticHelpOptional().ifPresent(lines::add);

        if (content.helpAvailable()) {
            optionalText(helpIndicator).ifPresent(lines::add);
        }

        return List.copyOf(lines);
    }

    /**
     * Composes tooltip content into a newline-separated string.
     *
     * <p>Use this convenience when a renderer has a single plain-text tooltip
     * slot. Use {@link #composeLines(TooltipContent)} when the caller needs to
     * render each line separately.</p>
     *
     * @param content tooltip content
     * @return composed tooltip text, or empty when no parts are present
     */
    public Optional<String> compose(TooltipContent content) {
        List<String> lines = composeLines(content);
        if (lines.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(String.join("\n", lines));
    }

    /**
     * Returns the configured help indicator when present.
     *
     * @return help indicator, or empty
     */
    public Optional<String> helpIndicator() {
        return optionalText(helpIndicator);
    }

    private static Optional<String> optionalText(String text) {
        String normalized = normalize(text);
        return normalized.isBlank() ? Optional.empty() : Optional.of(normalized);
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String stripped = text.strip();
        return stripped.isBlank() ? "" : stripped;
    }
}
