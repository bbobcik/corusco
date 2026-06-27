package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.annotations.dataset.AggregationFunction;
import cz.auderis.corusco.annotations.dataset.CoruscoDataSet;
import cz.auderis.corusco.annotations.dataset.DataColumn;
import cz.auderis.corusco.annotations.dataset.DataColumnRole;
import cz.auderis.corusco.annotations.dataset.Dimension;
import cz.auderis.corusco.annotations.dataset.Measure;
import cz.auderis.corusco.annotations.dataset.MissingPolicy;
import cz.auderis.corusco.annotations.dataset.TimeAxis;
import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.table.Column;
import cz.auderis.corusco.annotations.table.CoruscoTable;
import java.math.BigDecimal;

@CoruscoTable(id = "showcase/timeseries")
@CoruscoDataSet(id = "showcase/timeseries")
record TimeseriesObservation(
        @DataColumn(role = DataColumnRole.SEQUENCE)
        @Column(width = 70)
        long sequence,
        @TimeAxis(unit = "millis", monotonic = true)
        @Column(width = 190, tooltip = "showcase/timeseries/time/tooltip")
        @Help(topic = "showcase/timeseries/time")
        long timestampMillis,
        @Dimension
        @Column(width = 90)
        String symbol,
        @Dimension
        @Column(width = 80)
        String venue,
        @Dimension
        @Column(width = 80)
        String region,
        @Dimension
        @Column(width = 100)
        String channel,
        @Dimension
        @Column(width = 90, tooltip = "showcase/timeseries/state/tooltip")
        @Help(topic = "showcase/timeseries/state")
        ObservationState state,
        @Measure(unit = "USD")
        @Column(width = 90)
        BigDecimal bid,
        @Measure(unit = "USD")
        @Column(width = 90)
        BigDecimal ask,
        @Measure(unit = "USD")
        @Column(width = 90)
        BigDecimal lastPrice,
        @Measure(unit = "shares", aggregations = AggregationFunction.SUM)
        @Column(width = 90)
        long volume,
        @Measure(unit = "USD")
        @Column(width = 110)
        BigDecimal notional,
        @Measure(unit = "millis", missing = MissingPolicy.NAN, aggregations = AggregationFunction.AVERAGE)
        @Column(width = 90)
        double latencyMillis
) {
}
