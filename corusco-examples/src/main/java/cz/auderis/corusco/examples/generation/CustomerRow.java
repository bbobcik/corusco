package cz.auderis.corusco.examples.generation;

import cz.auderis.corusco.annotations.table.Column;
import cz.auderis.corusco.annotations.table.SwingTable;

@SwingTable(id = "book/customer/table")
public record CustomerRow(
        @Column(header = "customer/name", width = 180)
        String name,
        @Column(header = "customer/status", width = 90)
        String status) {
}
