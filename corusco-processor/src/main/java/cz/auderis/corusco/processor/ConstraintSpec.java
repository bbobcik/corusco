package cz.auderis.corusco.processor;

/**
 * Processor-internal description of one generated validation constraint.
 *
 * <p>The spec is built from source annotations after validation has succeeded
 * and is consumed by the generated-source writer. It carries the public
 * metadata kind, generated problem-code constant, stable problem id, and
 * optional bounds exactly as they should appear in generated Java source.</p>
 */
final class ConstraintSpec {

    final String kind;
    final String problemConstant;
    final String problemId;
    final String min;
    final String max;

    ConstraintSpec(String kind, String problemConstant, String problemId, String min, String max) {
        this.kind = kind;
        this.problemConstant = problemConstant;
        this.problemId = problemId;
        this.min = min;
        this.max = max;
    }
}
