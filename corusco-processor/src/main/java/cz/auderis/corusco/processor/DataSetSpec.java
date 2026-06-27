package cz.auderis.corusco.processor;

import java.util.List;

/**
 * Normalized description of one generated data-set source.
 */
final class DataSetSpec {

    final String id;
    final String ownerType;
    final List<DataSetColumnSpec> columns;

    DataSetSpec(String id, String ownerType, List<DataSetColumnSpec> columns) {
        this.id = id;
        this.ownerType = ownerType;
        this.columns = List.copyOf(columns);
    }
}
