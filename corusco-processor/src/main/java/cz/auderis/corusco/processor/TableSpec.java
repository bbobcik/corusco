package cz.auderis.corusco.processor;

import java.util.List;

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
