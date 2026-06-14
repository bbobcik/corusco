package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.problem.Problem;

/**
 * Sealed state describing whether a text field's raw text currently parses.
 *
 * <p>{@link TextFieldModel} keeps raw user input and semantic field value
 * separate. When parsing succeeds, {@link Parsed} carries both the raw text and
 * the current semantic value. When parsing fails, {@link Failed} carries the
 * invalid raw text, the previous semantic value that remains protected by the
 * model, and the typed parse {@link Problem} that can be shown near the field.</p>
 *
 * <p>Bindings normally display the raw text regardless of state and use the
 * problem set exposed by the text field model for feedback. Validators should
 * treat failed parse state as a reason to skip semantic validation for that
 * field; a malformed value should not produce duplicate parse and validation
 * problems.</p>
 *
 * @param <T> semantic value type
 */
public sealed interface ParseState<T> permits ParseState.Parsed, ParseState.Failed {

    /**
     * Parsed raw text with a current semantic value.
     *
     * <p>The value may be {@code null} when the converter accepts empty or
     * optional input.</p>
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
     * <p>The previous value is the semantic value that was current before this
     * raw text failed to parse. It allows a form model to preserve the last
     * valid value while the user repairs the text.</p>
     *
     * @param rawText invalid raw text
     * @param previousValue semantic value before the failed parse, possibly {@code null}
     * @param problem typed parse problem
     * @param <T> semantic value type
     */
    record Failed<T>(String rawText, T previousValue, Problem problem) implements ParseState<T> {
    }
}
