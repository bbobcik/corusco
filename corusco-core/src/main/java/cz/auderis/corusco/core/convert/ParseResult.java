package cz.auderis.corusco.core.convert;

import java.util.Objects;

/**
 * Result of parsing raw text into a semantic value.
 *
 * <p>Successful results may contain {@code null}. Failed results carry a
 * diagnostic message; typed problem construction is handled by text field
 * models because they know the target field key.</p>
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
