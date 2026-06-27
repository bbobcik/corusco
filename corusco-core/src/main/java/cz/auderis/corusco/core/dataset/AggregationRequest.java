package cz.auderis.corusco.core.dataset;

import cz.auderis.corusco.annotations.dataset.DataColumnRole;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Transport-neutral aggregation or downsampling request for a data-set schema.
 *
 * <p>The request names a descriptor, an optional time range, an optional bucket
 * width, and the measure aggregations the caller wants. It is intentionally a
 * validation and transport object: it can prove that requested columns belong
 * to the descriptor and that requested functions are allowed by those columns,
 * but it does not scan data or compute aggregates.</p>
 *
 * <p>Adapters can translate a request to SQL, generated-frame loops, file
 * scans, or remote service calls. A {@code null} bucket width means "aggregate
 * over the whole selected range" rather than downsample into regular buckets.
 * A {@code null} time range means the caller did not restrict the request by
 * time.</p>
 *
 * @param descriptor target data-set descriptor
 * @param timeRange optional time range
 * @param bucketWidth optional bucket width for downsampling
 * @param measures requested measure aggregations
 * @param <R> source row type
 */
public record AggregationRequest<R>(
        DataSetDescriptor<R> descriptor,
        @Nullable TimeRange timeRange,
        @Nullable Duration bucketWidth,
        List<MeasureAggregation<R, ?>> measures
) {

    /**
     * Creates a request.
     *
     * @param descriptor target descriptor
     * @param timeRange optional time range, or {@code null} for unrestricted
     *        descriptor scope
     * @param bucketWidth optional positive bucket width, or {@code null} when
     *        no regular downsampling buckets are requested
     * @param measures requested aggregations
     */
    public AggregationRequest {
        Objects.requireNonNull(descriptor, "descriptor");
        if (bucketWidth != null && (bucketWidth.isZero() || bucketWidth.isNegative())) {
            throw new IllegalArgumentException("bucketWidth must be positive");
        }
        measures = List.copyOf(Objects.requireNonNull(measures, "measures"));
        for (MeasureAggregation<R, ?> measure : measures) {
            if (!descriptor.columns().contains(measure.column())) {
                throw new IllegalArgumentException("measure column does not belong to descriptor: "
                        + measure.column().key().id());
            }
            if (measure.column().role() != DataColumnRole.MEASURE) {
                throw new IllegalArgumentException("aggregation column must be a measure: "
                        + measure.column().key().id());
            }
            if (!measure.column().aggregationFunctions().contains(measure.function())) {
                throw new IllegalArgumentException("aggregation function is not allowed for column "
                        + measure.column().key().id() + ": " + measure.function());
            }
        }
        if (timeRange != null && descriptor.timeAxis().isEmpty()) {
            throw new IllegalArgumentException("time range requires a descriptor with a time axis");
        }
    }
}
