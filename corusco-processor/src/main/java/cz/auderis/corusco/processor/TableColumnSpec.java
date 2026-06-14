package cz.auderis.corusco.processor;

/**
 * Processor-internal description of one generated table column.
 *
 * <p>The spec is built from a record component annotated with {@code @Column}
 * after ids, resource ids, width bounds, visibility, capabilities, help topic,
 * and persistence metadata have been resolved. {@link TableSourceWriter}
 * consumes it to generate column keys, descriptors, row accessors, and optional
 * immutable-row updater functions.</p>
 */
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
    final String helpTopicId;
    final String persistenceId;
    final int width;
    final int minWidth;
    final int maxWidth;
    final int order;
    final boolean visible;
    final boolean sortable;
    final boolean filterable;
    final boolean hideable;
    final boolean editable;

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
            String helpTopicId,
            String persistenceId,
            int width,
            int minWidth,
            int maxWidth,
            int order,
            boolean visible,
            boolean sortable,
            boolean filterable,
            boolean hideable,
            boolean editable
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
        this.helpTopicId = helpTopicId;
        this.persistenceId = persistenceId;
        this.width = width;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.order = order;
        this.visible = visible;
        this.sortable = sortable;
        this.filterable = filterable;
        this.hideable = hideable;
        this.editable = editable;
    }
}
