package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.table.Column;
import cz.auderis.corusco.annotations.table.CoruscoTable;
import java.math.BigDecimal;

@CoruscoTable(id = "showcase/timeseries")
record TimeseriesObservation(
        @Column(width = 70)
        long sequence,
        @Column(width = 190, tooltip = "showcase/timeseries/time/tooltip")
        @Help(topic = "showcase/timeseries/time")
        long timestampMillis,
        @Column(width = 90)
        String symbol,
        @Column(width = 80)
        String venue,
        @Column(width = 80)
        String region,
        @Column(width = 100)
        String channel,
        @Column(width = 90, tooltip = "showcase/timeseries/state/tooltip")
        @Help(topic = "showcase/timeseries/state")
        ObservationState state,
        @Column(width = 90)
        BigDecimal bid,
        @Column(width = 90)
        BigDecimal ask,
        @Column(width = 90)
        BigDecimal lastPrice,
        @Column(width = 90)
        long volume,
        @Column(width = 110)
        BigDecimal notional,
        @Column(width = 90)
        double latencyMillis
) {
}
