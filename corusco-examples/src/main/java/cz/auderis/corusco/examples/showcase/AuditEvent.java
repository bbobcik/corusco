package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.table.Column;
import cz.auderis.corusco.annotations.table.SwingTable;

@SwingTable(id = "showcase/events")
record AuditEvent(
        @Column(width = 190, tooltip = "showcase/events/time/tooltip")
        @Help(topic = "showcase/events/time")
        Long timestampMillis,
        @Column(width = 100, tooltip = "showcase/events/state/tooltip")
        @Help(topic = "showcase/events/state")
        ObservationState state,
        @Column(width = 440)
        String message
) {
}
