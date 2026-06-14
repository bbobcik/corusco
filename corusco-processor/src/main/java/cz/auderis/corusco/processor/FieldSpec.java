package cz.auderis.corusco.processor;

import java.util.List;

/**
 * Normalized description of one generated form field.
 *
 * <p>The annotation processor builds this package-private model after
 * validating annotation combinations and supported component types. Writers use
 * it as the single source for generated field keys, resource keys, component
 * factory methods, converter wiring, and validation descriptors. The values are
 * already source-code fragments or stable ids; writer code should not reinterpret
 * annotation defaults independently.</p>
 */
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
    final List<String> enumOptionConstants;
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
            List<String> enumOptionConstants,
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
        this.enumOptionConstants = List.copyOf(enumOptionConstants);
        this.constraints = List.copyOf(constraints);
    }
}
