package cz.auderis.corusco.core.convert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.function.Function;

/**
 * Provides standard {@link StringConverter} implementations for form models.
 *
 * <p>Generated form models use this class to wire supported text field kinds to
 * semantic values, and handwritten forms can use the same converters for
 * consistent parse behavior. Converters return {@link ParseResult} instances
 * instead of throwing for user-entered text, allowing invalid intermediate UI
 * input to remain in a {@code TextFieldModel} while the last valid semantic
 * value is preserved.</p>
 *
 * <p>The converters are locale-neutral. Numbers use JDK parsing for the target
 * type, dates use ISO-8601 {@code yyyy-MM-dd}, and enum values use enum
 * constant names. Empty-text behavior is controlled by
 * {@link EmptyTextPolicy} for types where empty text may represent either a
 * missing value or a validation error.</p>
 */
public final class Converters {

    private Converters() {
    }

    /**
     * Creates a string converter. Empty text remains an empty string.
     *
     * @return string converter
     */
    public static StringConverter<String> string() {
        return new StringConverter<>() {
            @Override
            public Class<String> valueType() {
                return String.class;
            }

            @Override
            public ParseResult<String> parse(String rawText) {
                return rawText == null
                        ? ParseResult.failure("Text must not be null")
                        : ParseResult.success(rawText);
            }

            @Override
            public String format(String value) {
                return value == null ? "" : value;
            }
        };
    }

    /**
     * Creates an integer converter that rejects empty text.
     *
     * @return integer converter
     */
    public static StringConverter<Integer> integer() {
        return integer(EmptyTextPolicy.REJECT);
    }

    /**
     * Creates an integer converter.
     *
     * @param emptyTextPolicy empty-text policy
     * @return integer converter
     */
    public static StringConverter<Integer> integer(EmptyTextPolicy emptyTextPolicy) {
        return number(Integer.class, emptyTextPolicy, Integer::valueOf);
    }

    /**
     * Creates a long converter.
     *
     * @param emptyTextPolicy empty-text policy
     * @return long converter
     */
    public static StringConverter<Long> longValue(EmptyTextPolicy emptyTextPolicy) {
        return number(Long.class, emptyTextPolicy, Long::valueOf);
    }

    /**
     * Creates a BigDecimal converter.
     *
     * @param emptyTextPolicy empty-text policy
     * @return BigDecimal converter
     */
    public static StringConverter<BigDecimal> bigDecimal(EmptyTextPolicy emptyTextPolicy) {
        return number(BigDecimal.class, emptyTextPolicy, BigDecimal::new);
    }

    /**
     * Creates an ISO-8601 LocalDate converter.
     *
     * @param emptyTextPolicy empty-text policy
     * @return LocalDate converter
     */
    public static StringConverter<LocalDate> localDate(EmptyTextPolicy emptyTextPolicy) {
        return new BasicConverter<>(
                LocalDate.class,
                emptyTextPolicy,
                raw -> {
                    try {
                        return LocalDate.parse(raw);
                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("Expected ISO date yyyy-MM-dd", e);
                    }
                },
                value -> value == null ? "" : value.toString()
        );
    }

    /**
     * Creates an enum converter using enum constant names.
     *
     * @param enumType enum type
     * @param emptyTextPolicy empty-text policy
     * @param <E> enum type
     * @return enum converter
     */
    public static <E extends Enum<E>> StringConverter<E> enumValue(
            Class<E> enumType,
            EmptyTextPolicy emptyTextPolicy
    ) {
        Objects.requireNonNull(enumType, "enumType");
        return new BasicConverter<>(
                enumType,
                emptyTextPolicy,
                raw -> {
                    try {
                        return Enum.valueOf(enumType, raw);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Expected one of " + String.join(", ", enumNames(enumType)), e);
                    }
                },
                value -> value == null ? "" : value.name()
        );
    }

    private static <T> StringConverter<T> number(
            Class<T> valueType,
            EmptyTextPolicy emptyTextPolicy,
            Function<String, T> parser
    ) {
        return new BasicConverter<>(valueType, emptyTextPolicy, raw -> {
            try {
                return parser.apply(raw);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Expected " + valueType.getSimpleName(), e);
            }
        }, value -> value == null ? "" : value.toString());
    }

    private static <E extends Enum<E>> String[] enumNames(Class<E> enumType) {
        E[] constants = enumType.getEnumConstants();
        String[] names = new String[constants.length];
        for (int i = 0; i < constants.length; i++) {
            names[i] = constants[i].name();
        }
        return names;
    }

    private record BasicConverter<T>(
            Class<T> valueType,
            EmptyTextPolicy emptyTextPolicy,
            Function<String, T> parser,
            Function<T, String> formatter
    ) implements StringConverter<T> {

        private BasicConverter {
            Objects.requireNonNull(valueType, "valueType");
            Objects.requireNonNull(emptyTextPolicy, "emptyTextPolicy");
            Objects.requireNonNull(parser, "parser");
            Objects.requireNonNull(formatter, "formatter");
        }

        @Override
        public ParseResult<T> parse(String rawText) {
            if (rawText == null) {
                return ParseResult.failure("Text must not be null");
            }
            if (rawText.isEmpty()) {
                if (emptyTextPolicy == EmptyTextPolicy.NULL_VALUE) {
                    return ParseResult.success(null);
                }
                return ParseResult.failure("Value is required");
            }
            try {
                return ParseResult.success(parser.apply(rawText));
            } catch (IllegalArgumentException e) {
                return ParseResult.failure(e.getMessage());
            }
        }

        @Override
        public String format(T value) {
            return formatter.apply(value);
        }
    }
}
