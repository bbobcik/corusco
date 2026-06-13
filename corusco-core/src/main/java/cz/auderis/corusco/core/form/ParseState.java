package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.problem.Problem;

/**
 * Current parse state of a text field model.
 *
 * <p>Invalid intermediate input is represented by {@link Failed}; it keeps both
 * the raw text and the previous semantic value so UI code can display the raw
 * user input without destroying valid model state.</p>
 *
 * @param <T> semantic value type
 */
public sealed interface ParseState<T> permits ParseState.Parsed, ParseState.Failed {

    /**
     * Parsed raw text with a current semantic value.
     *
     * @param rawText raw text
     * @param value semantic value, possibly {@code null}
     * @param <T> semantic value type
     */
    record Parsed<T>(String rawText, T value) implements ParseState<T> {
    }

    /**
     * Failed raw text with the previous semantic value and typed parse problem.
     *
     * @param rawText invalid raw text
     * @param previousValue semantic value before the failed parse, possibly {@code null}
     * @param problem typed parse problem
     * @param <T> semantic value type
     */
    record Failed<T>(String rawText, T previousValue, Problem problem) implements ParseState<T> {
    }
}
