package cz.auderis.corusco.processor;

final class TableColumnSpec {

    final String constantName;
    final String keyId;
    final String componentName;
    final String ownerType;
    final String valueType;
    final String valueClass;
    final String headerConstant;
    final String headerId;
    final String tooltipConstant;
    final String tooltipId;
    final int width;
    final int order;
    final boolean visible;
    final boolean sortable;
    final boolean filterable;
    final boolean hideable;

    TableColumnSpec(
            String constantName,
            String keyId,
            String componentName,
            String ownerType,
            String valueType,
            String valueClass,
            String headerConstant,
            String headerId,
            String tooltipConstant,
            String tooltipId,
            int width,
            int order,
            boolean visible,
            boolean sortable,
            boolean filterable,
            boolean hideable
    ) {
        this.constantName = constantName;
        this.keyId = keyId;
        this.componentName = componentName;
        this.ownerType = ownerType;
        this.valueType = valueType;
        this.valueClass = valueClass;
        this.headerConstant = headerConstant;
        this.headerId = headerId;
        this.tooltipConstant = tooltipConstant;
        this.tooltipId = tooltipId;
        this.width = width;
        this.order = order;
        this.visible = visible;
        this.sortable = sortable;
        this.filterable = filterable;
        this.hideable = hideable;
    }
}
