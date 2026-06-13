package cz.auderis.corusco.core.problem;

import java.util.Objects;

/**
 * Stable identity for the producer of a problem.
 *
 * <p>Sources allow parse, validation, server, generated, and system feedback to
 * coexist while remaining filterable. Equality and hash code are based on the
 * stable id.</p>
 *
 * @param id stable non-blank source id
 */
public record ProblemSource(String id) {

    /**
     * Source used for parsing and conversion problems.
     */
    public static final ProblemSource PARSE = new ProblemSource("parse");

    /**
     * Source used for handwritten or generated validation problems.
     */
    public static final ProblemSource VALIDATION = new ProblemSource("validation");

    /**
     * Source used for server-side or external service feedback.
     */
    public static final ProblemSource SERVER = new ProblemSource("server");

    /**
     * Source used for framework/system feedback.
     */
    public static final ProblemSource SYSTEM = new ProblemSource("system");

    /**
     * Creates a problem source.
     *
     * @param id stable non-blank source id
     */
    public ProblemSource {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }

    /**
     * Creates a custom problem source.
     *
     * @param id stable non-blank source id
     * @return problem source
     */
    public static ProblemSource of(String id) {
        return new ProblemSource(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
