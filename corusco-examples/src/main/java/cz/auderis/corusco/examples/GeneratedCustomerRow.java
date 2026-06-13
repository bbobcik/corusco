package cz.auderis.corusco.examples;

import cz.auderis.corusco.annotations.Column;
import cz.auderis.corusco.annotations.SwingTable;

@SwingTable(id = "generated-customer-table")
record GeneratedCustomerRow(
        @Column(width = 180, tooltip = "generated-customer-table/name/help") String name,
        @Column(width = 80, sortable = false) int orders
) {
}
