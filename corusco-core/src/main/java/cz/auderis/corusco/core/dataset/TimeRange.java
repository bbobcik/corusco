package cz.auderis.corusco.core.dataset;

import java.time.Instant;
import java.util.Objects;

/**
 * Inclusive-exclusive time range for time-axis requests.
 *
 * <p>The range follows the usual half-open interval convention:
 * {@code startInclusive <= value < endExclusive}. This avoids ambiguity when
 * adjacent buckets or pages meet at the same instant. The constructor rejects
 * empty and reversed ranges.</p>
 *
 * @param startInclusive range start
 * @param endExclusive range end
 */
public record TimeRange(Instant startInclusive, Instant endExclusive) {

    /**
     * Creates a range.
     *
     * @param startInclusive range start
     * @param endExclusive range end
     */
    public TimeRange {
        Objects.requireNonNull(startInclusive, "startInclusive");
        Objects.requireNonNull(endExclusive, "endExclusive");
        if (!startInclusive.isBefore(endExclusive)) {
            throw new IllegalArgumentException("startInclusive must be before endExclusive");
        }
    }
}
