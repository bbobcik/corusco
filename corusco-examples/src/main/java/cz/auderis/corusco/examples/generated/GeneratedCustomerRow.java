package cz.auderis.corusco.examples.generated;

import cz.auderis.corusco.annotations.table.Column;
import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.table.SwingTable;

/**
 * Annotated sample row record used by generated table examples.
 *
 * <p>The record shows the split between row data and presentation metadata:
 * components store ordinary values, while {@link SwingTable}, {@link Column},
 * and {@link Help} provide stable table ids, column persistence ids, sizing,
 * editability, and tooltip/help resources for generated descriptors.</p>
 */
@SwingTable(id = "generated-customer-table")
public record GeneratedCustomerRow(
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
