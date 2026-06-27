package cz.auderis.corusco.core.dataset;

import cz.auderis.corusco.annotations.dataset.AggregationFunction;
import cz.auderis.corusco.annotations.dataset.DataColumnRole;
import cz.auderis.corusco.annotations.dataset.MissingPolicy;
import cz.auderis.corusco.annotations.dataset.QualityPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataSetDescriptorTest {

    private static final DataSetKey<Quote> QUOTES = DataSetKey.of("market/quotes", Quote.class);
    private static final DataColumnDescriptor<Quote, Long> TIME = new DataColumnDescriptor<>(
            DataColumnKey.of("market/quotes/timestamp", QUOTES, Long.class),
            "timestamp",
            DataColumnRole.TIME_AXIS,
            DataStorageType.LONG_ARRAY,
            UnitMetadata.of("millis"),
            MissingPolicy.NONE,
            QualityPolicy.NONE,
            Set.of()
    );
    private static final DataColumnDescriptor<Quote, String> SYMBOL = new DataColumnDescriptor<>(
            DataColumnKey.of("market/quotes/symbol", QUOTES, String.class),
            "symbol",
            DataColumnRole.DIMENSION,
            DataStorageType.OBJECT_ARRAY,
            null,
            MissingPolicy.NULL_VALUE,
            QualityPolicy.NONE,
            Set.of()
    );
    private static final DataColumnDescriptor<Quote, Double> BID = new DataColumnDescriptor<>(
            DataColumnKey.of("market/quotes/bid", QUOTES, Double.class),
            "bid",
            DataColumnRole.MEASURE,
            DataStorageType.DOUBLE_ARRAY,
            UnitMetadata.of("USD"),
            MissingPolicy.NAN,
            QualityPolicy.FLAGS,
            Set.of(AggregationFunction.MIN, AggregationFunction.MAX, AggregationFunction.AVERAGE)
    );

    @Test
    void keysValidateStableIdsAndTypes() {
        DataColumnKey<Quote, Double> key = DataColumnKey.of("market/quotes/bid", QUOTES, Double.class);

        assertThat(QUOTES.id()).isEqualTo("market/quotes");
        assertThat(QUOTES.rowType()).isEqualTo(Quote.class);
        assertThat(QUOTES).hasToString("DataSetKey[Quote#market/quotes]");
        assertThat(key.valueType()).isEqualTo(Double.class);
        assertThat(key).hasToString("DataColumnKey[Quote#market/quotes/bid:Double]");
        assertThatThrownBy(() -> DataSetKey.of(" ", Quote.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
        assertThatThrownBy(() -> DataColumnKey.of("bad id", QUOTES, Double.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
        assertThatThrownBy(() -> DataColumnKey.of("market/quotes/bid", QUOTES, double.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valueType");
    }

    @Test
    void descriptorGroupsSemanticColumns() {
        DataSetDescriptor<Quote> descriptor = new DataSetDescriptor<>(QUOTES, List.of(TIME, SYMBOL, BID));

        assertThat(descriptor.columns()).containsExactly(TIME, SYMBOL, BID);
        assertThat(descriptor.timeAxis()).contains(TIME);
        assertThat(descriptor.dimensions()).containsExactly(SYMBOL);
        assertThat(descriptor.measures()).containsExactly(BID);
        assertThatThrownBy(() -> descriptor.columns().add(BID))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void descriptorRejectsMismatchedDataSetKeysAndDuplicateTimeAxes() {
        DataSetKey<Quote> other = DataSetKey.of("market/other", Quote.class);
        DataColumnDescriptor<Quote, Long> otherTime = new DataColumnDescriptor<>(
                DataColumnKey.of("market/other/timestamp", other, Long.class),
                "timestamp",
                DataColumnRole.TIME_AXIS,
                DataStorageType.LONG_ARRAY,
                UnitMetadata.of("millis"),
                MissingPolicy.NONE,
                QualityPolicy.NONE,
                Set.of()
        );

        assertThatThrownBy(() -> new DataSetDescriptor<>(QUOTES, List.of(BID, otherTime)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data-set key");
        assertThatThrownBy(() -> new DataSetDescriptor<>(QUOTES, List.of(TIME, TIME)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("time axis");
    }

    @Test
    void columnDescriptorsValidateMissingPoliciesAndAggregationRole() {
        assertThat(BID.missingPolicy()).isEqualTo(MissingPolicy.NAN);
        assertThat(BID.qualityPolicy()).isEqualTo(QualityPolicy.FLAGS);

        assertThatThrownBy(() -> new DataColumnDescriptor<>(
                DataColumnKey.of("market/quotes/volume", QUOTES, Long.class),
                "volume",
                DataColumnRole.MEASURE,
                DataStorageType.LONG_ARRAY,
                UnitMetadata.of("shares"),
                MissingPolicy.NAN,
                QualityPolicy.NONE,
                Set.of(AggregationFunction.SUM)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NaN");
        assertThatThrownBy(() -> new DataColumnDescriptor<>(
                DataColumnKey.of("market/quotes/symbol", QUOTES, String.class),
                "symbol",
                DataColumnRole.DIMENSION,
                DataStorageType.OBJECT_ARRAY,
                null,
                MissingPolicy.NULL_VALUE,
                QualityPolicy.NONE,
                Set.of(AggregationFunction.COUNT)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("measure");
    }

    @Test
    void aggregationRequestsValidateShape() {
        DataSetDescriptor<Quote> descriptor = new DataSetDescriptor<>(QUOTES, List.of(TIME, SYMBOL, BID));
        AggregationRequest<Quote> request = new AggregationRequest<>(
                descriptor,
                new TimeRange(Instant.parse("2026-06-27T10:00:00Z"), Instant.parse("2026-06-27T11:00:00Z")),
                Duration.ofMinutes(5),
                List.of(new MeasureAggregation<>(BID, AggregationFunction.AVERAGE))
        );

        assertThat(request.measures()).hasSize(1);
        assertThatThrownBy(() -> new AggregationRequest<>(
                descriptor,
                null,
                Duration.ZERO,
                List.of()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bucketWidth");
        assertThatThrownBy(() -> new AggregationRequest<>(
                descriptor,
                null,
                null,
                List.of(new MeasureAggregation<>(BID, AggregationFunction.SUM))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not allowed");
        assertThatThrownBy(() -> new TimeRange(
                Instant.parse("2026-06-27T11:00:00Z"),
                Instant.parse("2026-06-27T10:00:00Z")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("before");
    }

    private record Quote(long timestamp, String symbol, double bid) {
    }
}
