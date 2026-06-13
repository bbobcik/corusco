package cz.auderis.corusco.core.validation;

import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Generated-compatible validation constraint helpers.
 */
public final class Validators {

    private static final ProblemCode REQUIRED = ProblemCode.of("validation/required");
    private static final ProblemCode LENGTH = ProblemCode.of("validation/length");
    private static final ProblemCode RANGE = ProblemCode.of("validation/range");
    private static final ProblemCode REGEX = ProblemCode.of("validation/regex");
    private static final ProblemCode DATE = ProblemCode.of("validation/date");

    private Validators() {
    }

    /**
     * Requires a non-null and, for strings, non-blank value.
     *
     * @param message diagnostic message
     * @param <O> owner/model type
     * @param <T> field value type
     * @return required validator
     */
    public static <O, T> FieldValidator<O, T> required(String message) {
        return (key, value) -> {
            boolean missing = value == null
                    || value instanceof String string && string.isBlank();
            return missing ? problem(REQUIRED, key, message) : ProblemSet.empty();
        };
    }

    /**
     * Restricts string length.
     *
     * @param min minimum length, inclusive
     * @param max maximum length, inclusive
     * @param message diagnostic message
     * @param <O> owner/model type
     * @return length validator
     */
    public static <O> FieldValidator<O, String> length(int min, int max, String message) {
        if (min < 0 || max < min) {
            throw new IllegalArgumentException("invalid length range");
        }
        return (key, value) -> {
            if (value == null) {
                return ProblemSet.empty();
            }
            int length = value.length();
            return length < min || length > max ? problem(LENGTH, key, message) : ProblemSet.empty();
        };
    }

    /**
     * Restricts a BigDecimal value.
     *
     * @param min minimum value, inclusive, or {@code null}
     * @param max maximum value, inclusive, or {@code null}
     * @param message diagnostic message
     * @param <O> owner/model type
     * @return decimal range validator
     */
    public static <O> FieldValidator<O, BigDecimal> decimalRange(BigDecimal min, BigDecimal max, String message) {
        return (key, value) -> {
            if (value == null) {
                return ProblemSet.empty();
            }
            boolean below = min != null && value.compareTo(min) < 0;
            boolean above = max != null && value.compareTo(max) > 0;
            return below || above ? problem(RANGE, key, message) : ProblemSet.empty();
        };
    }

    /**
     * Restricts an integer value.
     *
     * @param min minimum value, inclusive, or {@code null}
     * @param max maximum value, inclusive, or {@code null}
     * @param message diagnostic message
     * @param <O> owner/model type
     * @return integer range validator
     */
    public static <O> FieldValidator<O, Integer> integerRange(Integer min, Integer max, String message) {
        return (key, value) -> {
            if (value == null) {
                return ProblemSet.empty();
            }
            boolean below = min != null && value < min;
            boolean above = max != null && value > max;
            return below || above ? problem(RANGE, key, message) : ProblemSet.empty();
        };
    }

    /**
     * Requires a string to match a regular expression.
     *
     * @param pattern pattern to match
     * @param message diagnostic message
     * @param <O> owner/model type
     * @return regex validator
     */
    public static <O> FieldValidator<O, String> regex(Pattern pattern, String message) {
        Objects.requireNonNull(pattern, "pattern");
        return (key, value) -> {
            if (value == null || value.isEmpty()) {
                return ProblemSet.empty();
            }
            return pattern.matcher(value).matches() ? ProblemSet.empty() : problem(REGEX, key, message);
        };
    }

    /**
     * Requires a date to be before the current date.
     *
     * @param clock clock for deterministic tests
     * @param message diagnostic message
     * @param <O> owner/model type
     * @return past-date validator
     */
    public static <O> FieldValidator<O, LocalDate> past(Clock clock, String message) {
        Objects.requireNonNull(clock, "clock");
        return (key, value) -> value == null || value.isBefore(LocalDate.now(clock))
                ? ProblemSet.empty()
                : problem(DATE, key, message);
    }

    /**
     * Requires a date to be after the current date.
     *
     * @param clock clock for deterministic tests
     * @param message diagnostic message
     * @param <O> owner/model type
     * @return future-date validator
     */
    public static <O> FieldValidator<O, LocalDate> future(Clock clock, String message) {
        Objects.requireNonNull(clock, "clock");
        return (key, value) -> value == null || value.isAfter(LocalDate.now(clock))
                ? ProblemSet.empty()
                : problem(DATE, key, message);
    }

    /**
     * Requires a date to equal the current date.
     *
     * @param clock clock for deterministic tests
     * @param message diagnostic message
     * @param <O> owner/model type
     * @return present-date validator
     */
    public static <O> FieldValidator<O, LocalDate> present(Clock clock, String message) {
        Objects.requireNonNull(clock, "clock");
        return (key, value) -> value == null || value.equals(LocalDate.now(clock))
                ? ProblemSet.empty()
                : problem(DATE, key, message);
    }

    private static <O, T> ProblemSet problem(ProblemCode code, FieldKey<O, T> key, String message) {
        return ProblemSet.of(Problem.validation(
                code,
                ProblemSeverity.ERROR,
                ProblemTarget.field(key),
                message
        ));
    }
}
