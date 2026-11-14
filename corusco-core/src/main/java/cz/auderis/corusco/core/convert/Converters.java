package cz.auderis.corusco.core.convert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.DecimalStyle;
import java.time.format.FormatStyle;
import java.time.format.ResolverStyle;
import java.util.Locale;
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
 * <p>The default converters are locale-neutral. Numbers use JDK parsing for the target
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
     * Creates a localized integer converter. Decimal notation is accepted when
     * its value is integral and within range.
     * @param emptyTextPolicy empty-text policy
     * @param locale number locale
     * @return localized integer converter
     * @see #bigDecimal(EmptyTextPolicy, Locale)
     */
    public static StringConverter<Integer> integer(EmptyTextPolicy emptyTextPolicy, Locale locale) {
        return localizedNumber(Integer.class, emptyTextPolicy, locale, BigDecimal::intValueExact);
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
     * Creates a localized long converter. Decimal notation is accepted when
     * its value is integral and within range.
     * @param emptyTextPolicy empty-text policy
     * @param locale number locale
     * @return localized long converter
     * @see #bigDecimal(EmptyTextPolicy, Locale)
     */
    public static StringConverter<Long> longValue(EmptyTextPolicy emptyTextPolicy, Locale locale) {
        return localizedNumber(Long.class, emptyTextPolicy, locale, BigDecimal::longValueExact);
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
     * Creates a localized decimal converter. Parsing consumes the entire input;
     * whitespace is not trimmed. Grouping is optional, but when present must
     * match the locale's grouping size and separator. Non-finite values are rejected.
     * Formatting preserves all fractional digits without rounding.
     *
     * @param emptyTextPolicy empty-text policy
     * @param locale number locale
     * @return localized decimal converter
     */
    public static StringConverter<BigDecimal> bigDecimal(EmptyTextPolicy emptyTextPolicy, Locale locale) {
        return localizedNumber(BigDecimal.class, emptyTextPolicy, locale, Function.identity());
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
     * Creates a strict localized short-date converter. The locale's short pattern
     * is used with a proleptic year of at least four digits instead of a reduced
     * two-digit year, avoiding a moving century window.
     *
     * @param emptyTextPolicy empty-text policy
     * @param locale date locale
     * @return localized date converter
     */
    public static StringConverter<LocalDate> localDate(EmptyTextPolicy emptyTextPolicy, Locale locale) {
        Objects.requireNonNull(locale, "locale");
        String pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                FormatStyle.SHORT, null, IsoChronology.INSTANCE, locale);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fullYearPattern(pattern), locale)
                .withDecimalStyle(DecimalStyle.of(locale))
                .withResolverStyle(ResolverStyle.STRICT);
        return new BasicConverter<>(LocalDate.class, emptyTextPolicy, raw -> {
            try {
                return LocalDate.parse(raw, formatter);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Expected localized date", e);
            }
        }, value -> value == null ? "" : formatter.format(value));
    }

    private static String fullYearPattern(String pattern) {
        StringBuilder result = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '\'') {
                result.append(c);
                if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '\'') {
                    result.append(pattern.charAt(++i));
                } else {
                    quoted = !quoted;
                }
            } else if (!quoted && c == 'y') {
                result.append("uuuu");
                while (i + 1 < pattern.length() && pattern.charAt(i + 1) == 'y') i++;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private static <T extends Number> StringConverter<T> localizedNumber(
            Class<T> type, EmptyTextPolicy policy, Locale locale, Function<BigDecimal, T> conversion) {
        Objects.requireNonNull(locale, "locale");
        return new BasicConverter<>(type, policy, raw -> {
            DecimalFormat format = decimalFormat(locale);
            ParsePosition position = new ParsePosition(0);
            Number parsed = format.parse(raw, position);
            if (!(parsed instanceof BigDecimal decimal) || position.getIndex() != raw.length()) {
                throw new IllegalArgumentException("Expected " + type.getSimpleName());
            }
            try {
                return conversion.apply(decimal);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("Expected " + type.getSimpleName(), e);
            }
        }, value -> {
            if (value == null) return "";
            DecimalFormat format = decimalFormat(locale);
            if (value instanceof BigDecimal decimal) {
                int scale = Math.max(0, decimal.scale());
                format.setMaximumFractionDigits(scale);
                format.setMinimumFractionDigits(scale);
            }
            return format.format(value);
        });
    }

    private static DecimalFormat decimalFormat(Locale locale) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        if (!(numberFormat instanceof DecimalFormat format)) {
            throw new IllegalArgumentException("Locale does not provide a decimal format: " + locale);
        }
        format.setStrict(true);
        format.setParseBigDecimal(true);
        format.setMaximumIntegerDigits(Integer.MAX_VALUE);
        return format;
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
