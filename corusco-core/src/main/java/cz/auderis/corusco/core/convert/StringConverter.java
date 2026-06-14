package cz.auderis.corusco.core.convert;

/**
 * Bidirectional converter between editable text and a typed semantic value.
 *
 * <p>Text field models use converters to keep raw user input separate from the
 * domain value that a form commits. {@link #parse(String)} turns the current
 * editor text into a {@link ParseResult}; {@link #format(Object)} turns the
 * last accepted semantic value back into text when a model resets or is
 * initialized. This lets a field preserve invalid intermediate text and expose
 * a parse problem without losing the previous valid value.</p>
 *
 * <p>Converters are synchronous, Swing-free, and normally stateless. They
 * should document null and empty-string handling explicitly. Implementations
 * should not report validation failures such as "required" or "too long" from
 * parsing unless the text cannot be converted to the target type; semantic
 * validation belongs to validators and rule sets.</p>
 *
 * @param <T> semantic value type
 */
public interface StringConverter<T> {

    /**
     * Returns the semantic value type produced by this converter.
     *
     * @return value type
     */
    Class<T> valueType();

    /**
     * Parses raw text into a semantic value or a parse failure.
     *
     * <p>Implementations should return {@link ParseResult#failure(String)}
     * rather than throwing for expected invalid input. Throwing is reserved for
     * programmer errors or unrecoverable converter failures.</p>
     *
     * @param rawText raw text; implementations treat {@code null} as invalid
     * @return parse result
     */
    ParseResult<T> parse(String rawText);

    /**
     * Formats a semantic value for editing.
     *
     * <p>The returned string is written to text components and should be stable
     * enough that parse/format round trips do not surprise users during reset
     * or model initialization.</p>
     *
     * @param value semantic value, possibly {@code null}
     * @return raw text representation
     */
    String format(T value);
}
