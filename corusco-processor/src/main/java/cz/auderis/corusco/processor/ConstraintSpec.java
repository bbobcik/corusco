package cz.auderis.corusco.processor;

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
