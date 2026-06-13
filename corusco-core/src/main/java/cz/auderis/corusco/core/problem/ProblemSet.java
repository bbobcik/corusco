package cz.auderis.corusco.core.problem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Immutable deterministic collection of problems.
 *
 * <p>The set preserves insertion order for iteration and derives deterministic
 * views through explicit methods. Adding problems returns a new set. Filtering
 * never mutates the original set.</p>
 */
public final class ProblemSet {

    private static final Comparator<Problem> BY_SEVERITY_DESCENDING =
            Comparator.comparing(Problem::severity).reversed();

    private final List<Problem> problems;

    private ProblemSet(List<Problem> problems) {
        this.problems = List.copyOf(problems);
    }

    /**
     * Creates an empty problem set.
     *
     * @return empty problem set
     */
    public static ProblemSet empty() {
        return new ProblemSet(List.of());
    }

    /**
     * Creates a problem set from problems in iteration order.
     *
     * @param problems problems to include
     * @return immutable problem set
     */
    public static ProblemSet of(Problem... problems) {
        return new ProblemSet(List.of(problems));
    }

    /**
     * Creates a problem set from problems in iteration order.
     *
     * @param problems problems to include
     * @return immutable problem set
     */
    public static ProblemSet copyOf(Iterable<Problem> problems) {
        Objects.requireNonNull(problems, "problems");
        List<Problem> copy = new ArrayList<>();
        for (Problem problem : problems) {
            copy.add(Objects.requireNonNull(problem, "problem"));
        }
        return new ProblemSet(copy);
    }

    /**
     * Returns problems in insertion order.
     *
     * @return immutable problem list
     */
    public List<Problem> problems() {
        return problems;
    }

    /**
     * Indicates whether this set contains no problems.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return problems.isEmpty();
    }

    /**
     * Returns the number of problems.
     *
     * @return problem count
     */
    public int size() {
        return problems.size();
    }

    /**
     * Adds a problem and returns a new set.
     *
     * @param problem problem to add
     * @return new set containing the added problem
     */
    public ProblemSet add(Problem problem) {
        Objects.requireNonNull(problem, "problem");
        List<Problem> copy = new ArrayList<>(problems);
        copy.add(problem);
        return new ProblemSet(copy);
    }

    /**
     * Adds all problems from another set and returns a new set.
     *
     * @param other other problem set
     * @return combined problem set
     */
    public ProblemSet addAll(ProblemSet other) {
        Objects.requireNonNull(other, "other");
        List<Problem> copy = new ArrayList<>(problems);
        copy.addAll(other.problems);
        return new ProblemSet(copy);
    }

    /**
     * Filters this set and returns a new immutable set.
     *
     * @param filter filter to apply
     * @return filtered problem set
     */
    public ProblemSet filter(ProblemFilter filter) {
        Objects.requireNonNull(filter, "filter");
        List<Problem> filtered = problems.stream()
                .filter(filter)
                .toList();
        return new ProblemSet(filtered);
    }

    /**
     * Returns problems ordered from most severe to least severe.
     *
     * <p>Problems with the same severity retain their insertion order because
     * the Java stream sort used here is stable.</p>
     *
     * @return severity-ordered immutable list
     */
    public List<Problem> bySeverityDescending() {
        return problems.stream()
                .sorted(BY_SEVERITY_DESCENDING)
                .toList();
    }

    /**
     * Indicates whether any problem has severity {@link ProblemSeverity#ERROR}.
     *
     * @return {@code true} if the set contains an error
     */
    public boolean hasErrors() {
        return problems.stream()
                .anyMatch(problem -> problem.severity() == ProblemSeverity.ERROR);
    }
}
