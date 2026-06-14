package cz.auderis.corusco.core.convert;

import java.util.Objects;

/**
 * Sealed result of parsing raw text into a semantic value.
 *
 * <p>Converters use this type instead of throwing exceptions for ordinary user
 * input failures. A {@link Success} carries the converted value, which may be
 * {@code null} for optional fields. A {@link Failure} carries diagnostic text
 * describing why the raw text could not be converted. Text field models turn
 * failures into typed parse problems because they know the field key and the
 * current parse/validation boundary.</p>
 *
 * <p>The result is immutable and Swing-free. It does not decide whether a
 * value is required, in range, or semantically valid; those checks belong to
 * validators after parsing succeeds.</p>
 *
 * @param <T> semantic value type
 */
public sealed interface ParseResult<T> permits ParseResult.Success, ParseResult.Failure {

    /**
     * Successful parse result.
     *
     * @param value parsed semantic value, possibly {@code null}
     * @param <T> semantic value type
     */
    record Success<T>(T value) implements ParseResult<T> {
    }

    /**
     * Failed parse result.
     *
     * @param message diagnostic parse failure message
     * @param <T> semantic value type
     */
    record Failure<T>(String message) implements ParseResult<T> {

        /**
         * Creates a parse failure.
         *
         * @param message diagnostic parse failure message
         */
        public Failure {
            Objects.requireNonNull(message, "message");
            if (message.isBlank()) {
                throw new IllegalArgumentException("message must not be blank");
            }
        }
    }

    /**
     * Creates a successful parse result.
     *
     * @param value parsed value, possibly {@code null}
     * @param <T> semantic value type
     * @return successful result
     */
    static <T> ParseResult<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed parse result.
     *
     * @param message diagnostic parse failure message
     * @param <T> semantic value type
     * @return failed result
     */
    static <T> ParseResult<T> failure(String message) {
        return new Failure<>(message);
    }
}
