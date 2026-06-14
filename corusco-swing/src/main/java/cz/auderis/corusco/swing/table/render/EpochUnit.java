package cz.auderis.corusco.swing.table.render;

import java.time.Instant;

/**
 * Interprets numeric epoch values for timestamp rendering.
 *
 * <p>The constants describe the unit of a {@code long} or {@link Long} table
 * value before it is converted to an {@link Instant}. Choose the unit that
 * matches the data source exactly; the renderer does not inspect column names
 * or value magnitude to guess whether a number is seconds, milliseconds,
 * microseconds, or nanoseconds.</p>
 *
 * <p>The enum is part of the public renderer options rather than table
 * metadata. Changing a renderer from one unit to another changes only the
 * visual interpretation of the same model value; it does not mutate table data
 * or persisted column layout.</p>
 */
public enum EpochUnit {

    /**
     * Whole seconds since {@code 1970-01-01T00:00:00Z}.
     */
    SECONDS,

    /**
     * Whole milliseconds since {@code 1970-01-01T00:00:00Z}.
     */
    MILLIS,

    /**
     * Whole microseconds since {@code 1970-01-01T00:00:00Z}.
     */
    MICROS,

    /**
     * Whole nanoseconds since {@code 1970-01-01T00:00:00Z}.
     */
    NANOS;

    Instant toInstant(long epochValue) {
        return switch (this) {
            case SECONDS -> Instant.ofEpochSecond(epochValue);
            case MILLIS -> Instant.ofEpochMilli(epochValue);
            case MICROS -> Instant.ofEpochSecond(
                    Math.floorDiv(epochValue, 1_000_000L),
                    Math.floorMod(epochValue, 1_000_000L) * 1_000L
            );
            case NANOS -> Instant.ofEpochSecond(
                    Math.floorDiv(epochValue, 1_000_000_000L),
                    Math.floorMod(epochValue, 1_000_000_000L)
            );
        };
    }
}
