package cz.auderis.corusco.core.table;

import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemFilter;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemTarget;
import java.util.List;
import java.util.Objects;

/**
 * Helpers for targeting problems at typed table cells.
 *
 * <p>This utility bridges the generic problem model and descriptor-backed table
 * code. A table cell is identified by a row object or row identity and a typed
 * {@link ColumnKey}. The helpers create {@link
 * cz.auderis.corusco.core.problem.ProblemTarget.Cell} instances and matching
 * filters without exposing callers to the raw generic target type.</p>
 *
 * <p>The row match uses the row object's {@link Object#equals} contract.
 * Applications that need stable identity across immutable row replacement
 * should target problems at stable row identity objects rather than transient
 * row instances. Swing table decorators can then filter problems independently
 * of current sort order or view indexes.</p>
 */
public final class TableCellProblems {

    private TableCellProblems() {
        throw new AssertionError("No instances");
    }

    /**
     * Creates a typed table-cell problem target.
     *
     * @param row row object or row identity
     * @param column typed column key
     * @param <R> row type
     * @param <V> cell value type
     * @return cell target
     */
    public static <R, V> ProblemTarget.Cell<R, ColumnKey<R, V>> target(R row, ColumnKey<R, V> column) {
        return ProblemTarget.cell(
                Objects.requireNonNull(row, "row"),
                Objects.requireNonNull(column, "column")
        );
    }

    /**
     * Creates a filter for one typed table cell.
     *
     * @param row row object or row identity
     * @param column typed column key
     * @param <R> row type
     * @param <V> cell value type
     * @return cell problem filter
     */
    public static <R, V> ProblemFilter filter(R row, ColumnKey<R, V> column) {
        Objects.requireNonNull(row, "row");
        Objects.requireNonNull(column, "column");
        return ProblemFilter.cell(row, column);
    }

    /**
     * Returns problems for one typed table cell.
     *
     * @param problems problem set
     * @param row row object or row identity
     * @param column typed column key
     * @param <R> row type
     * @param <V> cell value type
     * @return filtered problems
     */
    public static <R, V> ProblemSet forCell(ProblemSet problems, R row, ColumnKey<R, V> column) {
        if (problems == null) {
            return ProblemSet.empty();
        }
        return problems.filter(filter(row, column));
    }

    /**
     * Returns the most severe problem for one typed table cell.
     *
     * @param problems problem set
     * @param row row object or row identity
     * @param column typed column key
     * @param <R> row type
     * @param <V> cell value type
     * @return first problem by descending severity, or {@code null}
     */
    public static <R, V> Problem mostSevere(ProblemSet problems, R row, ColumnKey<R, V> column) {
        List<Problem> ordered = forCell(problems, row, column).bySeverityDescending();
        return ordered.isEmpty() ? null : ordered.getFirst();
    }
}
