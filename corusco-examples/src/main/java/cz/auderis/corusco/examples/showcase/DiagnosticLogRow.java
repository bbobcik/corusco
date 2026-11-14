package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.annotations.table.Column;
import cz.auderis.corusco.annotations.table.CoruscoTable;

@CoruscoTable(id = "diagnostic/log")
record DiagnosticLogRow(@Column Long timestamp, @Column String level, @Column String message) { }
