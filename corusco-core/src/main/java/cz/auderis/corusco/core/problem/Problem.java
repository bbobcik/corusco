package cz.auderis.corusco.core.problem;

import java.util.Objects;

/**
 * Immutable validation, parse, or feedback problem.
 *
 * <p>A problem is fully typed by its target and categorized by a stable
 * {@link ProblemCode}, {@link ProblemSeverity}, and {@link ProblemSource}.
 * The message is diagnostic text for tests and logs; user-facing localization
 * belongs to resource descriptors and application presentation code.</p>
 *
 * @param code stable problem code
 * @param severity severity used for ordering and committability decisions
 * @param target typed problem target
 * @param source producer of the problem
 * @param message diagnostic message
 */
public record Problem(
        ProblemCode code,
        ProblemSeverity severity,
        ProblemTarget target,
        ProblemSource source,
        String message
) {

    /**
     * Creates a problem.
     *
     * @param code stable problem code
     * @param severity problem severity
     * @param target typed problem target
     * @param source producer of the problem
     * @param message diagnostic message
     */
    public Problem {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(message, "message");
    }

    /**
     * Creates a validation problem.
     *
     * @param code stable problem code
     * @param severity problem severity
     * @param target typed problem target
     * @param message diagnostic message
     * @return validation problem
     */
    public static Problem validation(
            ProblemCode code,
            ProblemSeverity severity,
            ProblemTarget target,
            String message
    ) {
        return new Problem(code, severity, target, ProblemSource.VALIDATION, message);
    }

    /**
     * Creates a parse problem.
     *
     * @param code stable problem code
     * @param target typed problem target
     * @param message diagnostic message
     * @return parse problem with {@link ProblemSeverity#ERROR}
     */
    public static Problem parse(ProblemCode code, ProblemTarget target, String message) {
        return new Problem(code, ProblemSeverity.ERROR, target, ProblemSource.PARSE, message);
    }
}
