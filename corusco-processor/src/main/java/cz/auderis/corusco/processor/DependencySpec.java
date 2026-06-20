package cz.auderis.corusco.processor;

import java.util.List;

/**
 * Normalized generated dependency metadata.
 */
final class DependencySpec {

    final String constantName;
    final String sourceFieldName;
    final String sourceFieldConstant;
    final boolean sourceTextField;
    final String targetStateModel;
    final List<String> valueExpressions;
    final String effect;

    DependencySpec(
            String constantName,
            String sourceFieldName,
            String sourceFieldConstant,
            boolean sourceTextField,
            String targetStateModel,
            List<String> valueExpressions,
            String effect
    ) {
        this.constantName = constantName;
        this.sourceFieldName = sourceFieldName;
        this.sourceFieldConstant = sourceFieldConstant;
        this.sourceTextField = sourceTextField;
        this.targetStateModel = targetStateModel;
        this.valueExpressions = List.copyOf(valueExpressions);
        this.effect = effect;
    }
}
