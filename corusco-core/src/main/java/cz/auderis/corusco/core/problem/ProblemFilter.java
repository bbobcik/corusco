package cz.auderis.corusco.core.problem;

import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.key.FieldKey;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Predicate-like filter for immutable problem collections.
 *
 * <p>Filters are composable and side-effect free. Provided factories cover the
 * target dimensions needed by parsing, validation, tables, and components
 * without accepting arbitrary field-name strings.</p>
 */
@FunctionalInterface
public interface ProblemFilter extends Predicate<Problem> {

    /**
     * A filter that accepts every problem.
     */
    ProblemFilter ALL = problem -> true;

    @Override
    boolean test(Problem problem);

    /**
     * Combines this filter with another filter using logical AND.
     *
     * @param other other filter
     * @return combined filter
     */
    default ProblemFilter and(ProblemFilter other) {
        Objects.requireNonNull(other, "other");
        return problem -> test(problem) && other.test(problem);
    }

    /**
     * Combines this filter with another filter using logical OR.
     *
     * @param other other filter
     * @return combined filter
     */
    default ProblemFilter or(ProblemFilter other) {
        Objects.requireNonNull(other, "other");
        return problem -> test(problem) || other.test(problem);
    }

    /**
     * Accepts problems with an exact severity.
     *
     * @param severity severity to match
     * @return severity filter
     */
    static ProblemFilter severity(ProblemSeverity severity) {
        Objects.requireNonNull(severity, "severity");
        return problem -> problem.severity() == severity;
    }

    /**
     * Accepts problems with severity greater than or equal to the supplied
     * minimum using {@link ProblemSeverity} declaration order.
     *
     * @param minimum minimum severity
     * @return severity threshold filter
     */
    static ProblemFilter severityAtLeast(ProblemSeverity minimum) {
        Objects.requireNonNull(minimum, "minimum");
        return problem -> problem.severity().compareTo(minimum) >= 0;
    }

    /**
     * Accepts form-wide problems.
     *
     * @return form-target filter
     */
    static ProblemFilter form() {
        return problem -> problem.target() instanceof ProblemTarget.Form;
    }

    /**
     * Accepts problems for an exact field key.
     *
     * @param key typed field key
     * @param <O> owner/model type
     * @param <T> field value type
     * @return field-target filter
     */
    static <O, T> ProblemFilter field(FieldKey<O, T> key) {
        Objects.requireNonNull(key, "key");
        return problem -> problem.target() instanceof ProblemTarget.Field<?, ?> field
                && field.key().equals(key);
    }

    /**
     * Accepts problems for an exact row identity.
     *
     * @param row row identity or row object
     * @return row-target filter
     */
    static ProblemFilter row(Object row) {
        Objects.requireNonNull(row, "row");
        return problem -> problem.target() instanceof ProblemTarget.Row<?> rowTarget
                && rowTarget.row().equals(row);
    }

    /**
     * Accepts problems for an exact cell identity.
     *
     * @param row row identity or row object
     * @param column column identity
     * @return cell-target filter
     */
    static ProblemFilter cell(Object row, Object column) {
        Objects.requireNonNull(row, "row");
        Objects.requireNonNull(column, "column");
        return problem -> problem.target() instanceof ProblemTarget.Cell<?, ?> cell
                && cell.row().equals(row)
                && cell.column().equals(column);
    }

    /**
     * Accepts problems for an exact component key.
     *
     * @param key typed component key
     * @param <C> component type
     * @return component-target filter
     */
    static <C> ProblemFilter component(ComponentKey<C> key) {
        Objects.requireNonNull(key, "key");
        return problem -> problem.target() instanceof ProblemTarget.Component<?> component
                && component.key().equals(key);
    }

    /**
     * Accepts problems from an exact source.
     *
     * @param source source to match
     * @return source filter
     */
    static ProblemFilter source(ProblemSource source) {
        Objects.requireNonNull(source, "source");
        return problem -> problem.source().equals(source);
    }
}
