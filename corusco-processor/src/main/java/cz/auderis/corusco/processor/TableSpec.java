package cz.auderis.corusco.processor;

import java.util.List;

/**
 * Normalized description of one generated table row type.
 *
 * <p>The spec keeps record components and generated columns separate. Component
 * entries describe constructor/accessor order for immutable row replacement;
 * column entries describe only components annotated as visible table columns.
 * Writers use this split to generate descriptor constants and editable column
 * updaters without reflecting over records at runtime.</p>
 */
final class TableSpec {

    final String id;
    final String ownerType;
    final List<TableComponentSpec> components;
    final List<TableColumnSpec> columns;

    TableSpec(String id, String ownerType, List<TableComponentSpec> components, List<TableColumnSpec> columns) {
        this.id = id;
        this.ownerType = ownerType;
        this.components = List.copyOf(components);
        this.columns = List.copyOf(columns);
    }
}
