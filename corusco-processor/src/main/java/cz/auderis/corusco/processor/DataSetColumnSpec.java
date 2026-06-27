package cz.auderis.corusco.processor;

/**
 * Processor-internal description of one generated data-set column.
 */
final class DataSetColumnSpec {

    final String constantName;
    final String keyId;
    final String componentName;
    final String ownerType;
    final String sourceType;
    final String valueType;
    final String valueClass;
    final String role;
    final String storage;
    final String unit;
    final String missingPolicy;
    final String qualityPolicy;
    final String aggregationsExpression;

    DataSetColumnSpec(
            String constantName,
            String keyId,
            String componentName,
            String ownerType,
            String sourceType,
            String valueType,
            String valueClass,
            String role,
            String storage,
            String unit,
            String missingPolicy,
            String qualityPolicy,
            String aggregationsExpression
    ) {
        this.constantName = constantName;
        this.keyId = keyId;
        this.componentName = componentName;
        this.ownerType = ownerType;
        this.sourceType = sourceType;
        this.valueType = valueType;
        this.valueClass = valueClass;
        this.role = role;
        this.storage = storage;
        this.unit = unit;
        this.missingPolicy = missingPolicy;
        this.qualityPolicy = qualityPolicy;
        this.aggregationsExpression = aggregationsExpression;
    }
}
