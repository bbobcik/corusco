package cz.auderis.corusco.core.problem;

/**
 * Severity of a problem.
 *
 * <p>The declaration order is the natural ordering from least severe to most
 * severe. Filters such as {@link ProblemFilter#severityAtLeast(ProblemSeverity)}
 * use this ordering. Form commit logic treats {@link #ERROR} as blocking, while
 * {@link #INFO} and {@link #WARNING} are presentation feedback unless a caller
 * applies a stricter policy.</p>
 */
public enum ProblemSeverity {

    /**
     * Informational feedback that does not block committing a form.
     */
    INFO,

    /**
     * Warning feedback that may deserve attention but is not fatal by itself.
     */
    WARNING,

    /**
     * Error feedback that normally blocks committing a form.
     */
    ERROR
}
