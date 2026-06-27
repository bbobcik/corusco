package cz.auderis.corusco.core.dataset;

import cz.auderis.corusco.annotations.dataset.DataColumnRole;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable descriptor for a fixed-schema data set.
 *
 * <p>The descriptor is the core, Swing-free schema authority for generated
 * time-series and tidy-data sources. It owns ordered column metadata and can
 * expose semantic subsets such as dimensions, measures, quality columns, and
 * the optional time axis.</p>
 *
 * <p>Descriptor order is significant. Generated frames, table models, exports,
 * and request UIs can use it as the canonical column order. Every column must
 * belong to the same {@link DataSetKey}; this prevents accidentally composing
 * columns from different generated records.</p>
 *
 * <p>A descriptor may omit a time axis for non-temporal tidy data. When it has
 * a time axis, it must have exactly one. Request models use that invariant to
 * reject time-range filters against descriptors that cannot interpret them.</p>
 *
 * @param key stable data-set key
 * @param columns ordered column descriptors
 * @param <R> source row type
 */
public record DataSetDescriptor<R>(DataSetKey<R> key, List<DataColumnDescriptor<R, ?>> columns) {

    /**
     * Creates a descriptor.
     *
     * @param key stable data-set key
     * @param columns ordered column descriptors; must be non-empty and must
     *        all belong to {@code key}
     */
    public DataSetDescriptor {
        Objects.requireNonNull(key, "key");
        columns = List.copyOf(Objects.requireNonNull(columns, "columns"));
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("columns must not be empty");
        }
        int timeAxisCount = 0;
        for (DataColumnDescriptor<R, ?> column : columns) {
            if (!column.key().dataSetKey().equals(key)) {
                throw new IllegalArgumentException("column data-set key does not match descriptor: "
                        + column.key().id());
            }
            if (column.role() == DataColumnRole.TIME_AXIS) {
                timeAxisCount++;
            }
        }
        if (timeAxisCount > 1) {
            throw new IllegalArgumentException("descriptor must not declare more than one time axis");
        }
    }

    /**
     * Returns the optional time-axis column.
     *
     * <p>The result is empty for non-temporal tidy data. When present, the
     * descriptor constructor guarantees that this is the only time-axis
     * column.</p>
     *
     * @return time-axis column
     */
    public Optional<DataColumnDescriptor<R, ?>> timeAxis() {
        return columns.stream()
                .filter(column -> column.role() == DataColumnRole.TIME_AXIS)
                .findFirst();
    }

    /**
     * Returns measure columns in descriptor order.
     *
     * <p>Measures are the only columns that can declare aggregation functions.
     * This helper is intended for request builders, exporters, and adapters
     * that need to present aggregatable values without scanning roles
     * repeatedly.</p>
     *
     * @return measure columns
     */
    public List<DataColumnDescriptor<R, ?>> measures() {
        return columns.stream()
                .filter(column -> column.role() == DataColumnRole.MEASURE)
                .toList();
    }

    /**
     * Returns dimension columns in descriptor order.
     *
     * <p>Dimensions identify or group observations. This helper keeps common
     * adapter code independent from generated companion classes.</p>
     *
     * @return dimension columns
     */
    public List<DataColumnDescriptor<R, ?>> dimensions() {
        return columns.stream()
                .filter(column -> column.role() == DataColumnRole.DIMENSION)
                .toList();
    }
}
