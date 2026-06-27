package cz.auderis.corusco.core.dataset;

import cz.auderis.corusco.annotations.dataset.AggregationFunction;
import cz.auderis.corusco.annotations.dataset.DataColumnRole;
import java.util.Objects;

/**
 * Requested aggregation for one measure column.
 *
 * <p>This record is a single term inside an {@link AggregationRequest}. It
 * pairs a measure column with one function that the descriptor declared as
 * meaningful. The constructor validates the column role immediately; the
 * surrounding request validates ownership and allowed functions against its
 * descriptor.</p>
 *
 * @param column measure column
 * @param function aggregation function
 * @param <R> source row type
 * @param <V> measure value type
 */
public record MeasureAggregation<R, V>(
        DataColumnDescriptor<R, V> column,
        AggregationFunction function
) {

    /**
     * Creates an aggregation term.
     *
     * @param column measure column; non-measure columns are rejected
     * @param function requested aggregation function
     */
    public MeasureAggregation {
        Objects.requireNonNull(column, "column");
        Objects.requireNonNull(function, "function");
        if (column.role() != DataColumnRole.MEASURE) {
            throw new IllegalArgumentException("column must be a measure");
        }
    }
}
