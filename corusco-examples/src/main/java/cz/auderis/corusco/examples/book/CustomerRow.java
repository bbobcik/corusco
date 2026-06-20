package cz.auderis.corusco.examples.book;

import cz.auderis.corusco.annotations.table.Column;
import cz.auderis.corusco.annotations.table.CoruscoTable;

@CoruscoTable(id = "book/customer/table")
public record CustomerRow(
        @Column(header = "customer/name", width = 180)
        String name,
        @Column(header = "customer/status", width = 90)
        String status) {
}
