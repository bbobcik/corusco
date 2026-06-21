package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.convert.ParseResult;
import cz.auderis.corusco.core.convert.StringConverter;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.core.value.ChangeOrigin;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.ReadableValue;
import cz.auderis.corusco.core.value.SimpleValue;
import java.util.Objects;

/**
 * Text-editable field model that preserves invalid intermediate input.
 *
 * <p>This model is used when the UI edits text but the form result needs a
 * semantic Java value such as a number, date, or string. Raw text and semantic
 * value are separate. Parse success updates the semantic value and clears parse
 * problems. Parse failure stores the raw text, keeps the previous semantic
 * value, marks the field touched, and exposes a typed parse problem targeting
 * the field key.</p>
 *
 * <p>The model owns observable raw text, parse state, dirty state, touched
 * state, and parse problems. It delegates conversion to a {@link
 * StringConverter}; validation rules run outside this class after a semantic
 * value exists. This lets Swing bindings display exactly what the user typed
 * while the form model continues to protect the last valid value.</p>
 *
 * <p>Instances are mutable, synchronous, Swing-free, and not synchronized.
 * Component bindings are responsible for calling {@link #setRawText(String,
 * ChangeOrigin)} on the correct thread and closing any subscriptions they
 * install.</p>
 *
 * @param <O> owner/model type
 * @param <T> semantic value type
 */
public final class TextFieldModel<O, T> {

    private static final ProblemCode PARSE_FAILED = ProblemCode.of("parse/failed");

    private final TextFieldKey<O, T> key;
    private final StringConverter<T> converter;
    private final FieldModel<O, T> field;
    private final SimpleValue<String> rawText;
    private final SimpleValue<ParseState<T>> parseState;
    private final SimpleValue<ProblemSet> problems = SimpleValue.of(ProblemSet.empty());

    /**
     * Creates a text field model.
     *
     * @param key typed text field key
     * @param originalValue original semantic value, possibly {@code null}
     * @param converter string converter
     */
    public TextFieldModel(TextFieldKey<O, T> key, T originalValue, StringConverter<T> converter) {
        this.key = Objects.requireNonNull(key, "key");
        this.converter = Objects.requireNonNull(converter, "converter");
        this.field = new FieldModel<>(key.asFieldKey(), originalValue);
        String initialText = converter.format(originalValue);
        this.rawText = SimpleValue.of(initialText);
        this.parseState = SimpleValue.of(new ParseState.Parsed<>(initialText, originalValue));
    }

    /**
     * Returns the typed text field key.
     *
     * @return text field key
     */
    public TextFieldKey<O, T> key() {
        return key;
    }

    /**
     * Returns this text key as a general field key.
     *
     * @return field key
     */
    public FieldKey<O, T> fieldKey() {
        return key.asFieldKey();
    }

    /**
     * Returns the current semantic value.
     *
     * @return semantic value, possibly {@code null}
     */
    public T value() {
        return field.value().value();
    }

    /**
     * Returns observable raw text.
     *
     * @return raw text value
     */
    public ReadableValue<String> rawText() {
        return rawText;
    }

    /**
     * Returns observable parse state.
     *
     * @return parse state value
     */
    public ReadableValue<ParseState<T>> parseState() {
        return parseState;
    }

    /**
     * Returns observable dirty state.
     *
     * @return dirty value
     */
    public ReadableValue<Boolean> dirty() {
        return field.dirty();
    }

    /**
     * Returns observable touched state.
     *
     * @return touched value
     */
    public ReadableValue<Boolean> touched() {
        return field.touched();
    }

    /**
     * Returns current parse problems for this field.
     *
     * @return problem set
     */
    public ProblemSet problems() {
        return problems.value();
    }

    /**
     * Returns observable parse problems for this field.
     *
     * @return observable problem set
     */
    public ReadableValue<ProblemSet> problemSet() {
        return problems;
    }

    /**
     * Sets raw text and parses it synchronously.
     *
     * @param newRawText new raw text
     * @param origin change origin
     */
    public void setRawText(String newRawText, ChangeOrigin origin) {
        Objects.requireNonNull(newRawText, "newRawText");
        Objects.requireNonNull(origin, "origin");
        rawText.setValue(newRawText, origin);
        ParseResult<T> result = converter.parse(newRawText);
        if (result instanceof ParseResult.Success<T> success) {
            field.setValue(success.value(), origin);
            parseState.setValue(new ParseState.Parsed<>(newRawText, success.value()), origin);
            problems.setValue(ProblemSet.empty(), origin);
        } else if (result instanceof ParseResult.Failure<T> failure) {
            Problem problem = Problem.parse(PARSE_FAILED, ProblemTarget.field(fieldKey()), failure.message());
            parseState.setValue(new ParseState.Failed<>(newRawText, field.value().value(), problem), origin);
            problems.setValue(ProblemSet.of(problem), origin);
            field.markTouched(origin);
        }
    }

    /**
     * Resets raw text, semantic value, touched state, dirty state, and parse
     * problems to the original baseline.
     */
    public void reset() {
        field.reset();
        String resetText = converter.format(field.value().value());
        rawText.setValue(resetText, StandardChangeOrigin.SYSTEM);
        parseState.setValue(new ParseState.Parsed<>(resetText, field.value().value()), StandardChangeOrigin.SYSTEM);
        problems.setValue(ProblemSet.empty(), StandardChangeOrigin.SYSTEM);
    }

    /**
     * Accepts the current semantic value as the new dirty-state baseline.
     */
    public void acceptCurrentValue() {
        field.acceptCurrentValue();
    }

    /**
     * Indicates whether the field is dirty.
     *
     * @return dirty flag
     */
    public boolean isDirty() {
        return field.isDirty();
    }

    /**
     * Indicates whether the field has been touched.
     *
     * @return touched flag
     */
    public boolean isTouched() {
        return field.isTouched();
    }

    FieldModel<O, T> semanticField() {
        return field;
    }

}
