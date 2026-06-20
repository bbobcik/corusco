package cz.auderis.corusco.core.meta;

import cz.auderis.corusco.core.problem.ProblemCode;
import java.util.Objects;

/**
 * Immutable generated metadata for one field constraint.
 *
 * <p>Descriptors are intentionally declarative: they name the problem code and
 * constraint parameters, while generated form-model code decides how to
 * instantiate validators from this metadata.</p>
 *
 * <p>Generated {@code @CoruscoForm} records create these descriptors inside
 * descriptor companions such as {@code CustomerEditDescriptors} from validation annotations such as
 * {@code @Required}, {@code @Length}, {@code @Regex}, {@code @DecimalRange},
 * and {@code @IntRange}. They use problem-code constants from
 * companions such as {@code CustomerEditProblems}.</p>
 *
 * @param kind constraint kind
 * @param problemCode stable generated problem code
 * @param min optional lower bound
 * @param max optional upper bound
 */
public record ConstraintDescriptor(
        ConstraintKind kind,
        ProblemCode problemCode,
        String min,
        String max
) {

    /**
     * Creates constraint metadata.
     *
     * @param kind constraint kind
     * @param problemCode stable generated problem code
     * @param min optional lower bound
     * @param max optional upper bound
     */
    public ConstraintDescriptor {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(problemCode, "problemCode");
    }

    /**
     * Creates required constraint metadata.
     *
     * @param problemCode problem code
     * @return descriptor
     */
    public static ConstraintDescriptor required(ProblemCode problemCode) {
        return new ConstraintDescriptor(ConstraintKind.REQUIRED, problemCode, null, null);
    }

    /**
     * Creates string length constraint metadata.
     *
     * @param problemCode problem code
     * @param min minimum length
     * @param max maximum length
     * @return descriptor
     */
    public static ConstraintDescriptor length(ProblemCode problemCode, int min, int max) {
        return new ConstraintDescriptor(ConstraintKind.LENGTH, problemCode, Integer.toString(min), Integer.toString(max));
    }

    /**
     * Creates decimal range constraint metadata.
     *
     * @param problemCode problem code
     * @param min optional minimum value
     * @param max optional maximum value
     * @return descriptor
     */
    public static ConstraintDescriptor decimalRange(ProblemCode problemCode, String min, String max) {
        return new ConstraintDescriptor(ConstraintKind.DECIMAL_RANGE, problemCode, min, max);
    }

    /**
     * Creates integer range constraint metadata.
     *
     * @param problemCode problem code
     * @param min minimum value
     * @param max maximum value
     * @return descriptor
     */
    public static ConstraintDescriptor intRange(ProblemCode problemCode, int min, int max) {
        return new ConstraintDescriptor(ConstraintKind.INT_RANGE, problemCode, Integer.toString(min), Integer.toString(max));
    }

    /**
     * Creates regular-expression constraint metadata.
     *
     * @param problemCode problem code
     * @param pattern pattern text
     * @return descriptor
     */
    public static ConstraintDescriptor regex(ProblemCode problemCode, String pattern) {
        return new ConstraintDescriptor(ConstraintKind.REGEX, problemCode, Objects.requireNonNull(pattern, "pattern"), null);
    }
}
