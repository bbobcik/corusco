package cz.auderis.corusco.processor;

import java.util.List;

final class FieldSpec {

    final String constantName;
    final String keyId;
    final String componentName;
    final String ownerType;
    final String valueType;
    final String valueClass;
    final String kind;
    final String labelConstant;
    final String tooltipConstant;
    final String tooltipId;
    final String helpTopicId;
    final boolean textField;
    final String modelType;
    final String converterExpression;
    final String viewComponentType;
    final String viewMethodName;
    final List<ConstraintSpec> constraints;

    FieldSpec(
            String constantName,
            String keyId,
            String componentName,
            String ownerType,
            String valueType,
            String valueClass,
            String kind,
            String labelConstant,
            String tooltipConstant,
            String tooltipId,
            String helpTopicId,
            boolean textField,
            String modelType,
            String converterExpression,
            String viewComponentType,
            String viewMethodName,
            List<ConstraintSpec> constraints
    ) {
        this.constantName = constantName;
        this.keyId = keyId;
        this.componentName = componentName;
        this.ownerType = ownerType;
        this.valueType = valueType;
        this.valueClass = valueClass;
        this.kind = kind;
        this.labelConstant = labelConstant;
        this.tooltipConstant = tooltipConstant;
        this.tooltipId = tooltipId;
        this.helpTopicId = helpTopicId;
        this.textField = textField;
        this.modelType = modelType;
        this.converterExpression = converterExpression;
        this.viewComponentType = viewComponentType;
        this.viewMethodName = viewMethodName;
        this.constraints = List.copyOf(constraints);
    }
}
