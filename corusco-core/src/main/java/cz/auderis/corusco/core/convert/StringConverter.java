package cz.auderis.corusco.core.convert;

/**
 * Converts between raw text and a semantic value.
 *
 * <p>Converters are pure, Swing-free, and synchronous. They define null and
 * empty-text policy explicitly so text fields can preserve invalid intermediate
 * input while keeping the last valid semantic value.</p>
 *
 * @param <T> semantic value type
 */
public interface StringConverter<T> {

    /**
     * Returns the semantic value type.
     *
     * @return value type
     */
    Class<T> valueType();

    /**
     * Parses raw text.
     *
     * @param rawText raw text; implementations treat {@code null} as invalid
     * @return parse result
     */
    ParseResult<T> parse(String rawText);

    /**
     * Formats a semantic value for editing.
     *
     * @param value semantic value, possibly {@code null}
     * @return raw text representation
     */
    String format(T value);
}
