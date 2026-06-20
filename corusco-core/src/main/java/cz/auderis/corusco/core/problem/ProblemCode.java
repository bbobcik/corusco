package cz.auderis.corusco.core.problem;

import java.util.Objects;

/**
 * Stable identity for a kind of problem.
 *
 * <p>The id is a boundary string for diagnostics, generated validation code,
 * and resource/localization lookup. It is not a field name or expression.
 * Equality and hash code are based on the stable id.</p>
 *
 * <p>Generated {@code @CoruscoForm} records create {@code ProblemCode} constants
 * in problem-code companions such as {@code CustomerEditProblems} for supported validation annotations. Generated
 * field descriptors and generated form-model validation rules then refer to the
 * same constants, keeping diagnostics and descriptor metadata aligned.</p>
 *
 * @param id stable non-blank problem code id
 */
public record ProblemCode(String id) {

    /**
     * Creates a problem code.
     *
     * @param id stable non-blank problem code id
     */
    public ProblemCode {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }

    /**
     * Creates a problem code for handwritten or generated-style code.
     *
     * @param id stable non-blank problem code id
     * @return problem code
     */
    public static ProblemCode of(String id) {
        return new ProblemCode(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
