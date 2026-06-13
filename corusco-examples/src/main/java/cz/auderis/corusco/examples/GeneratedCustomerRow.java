package cz.auderis.corusco.examples;

import cz.auderis.corusco.annotations.Column;
import cz.auderis.corusco.annotations.Help;
import cz.auderis.corusco.annotations.SwingTable;

@SwingTable(id = "generated-customer-table")
record GeneratedCustomerRow(
        @Column(
                persistenceId = "generated-customer-table/customer-name",
                width = 180,
                minWidth = 80,
                maxWidth = 320,
                editable = true
        )
        @Help(tooltip = "generated-customer-table/name/help", topic = "generated-customer-table/name")
        String name,
        @Column(width = 80, sortable = false) int orders
) {
}
