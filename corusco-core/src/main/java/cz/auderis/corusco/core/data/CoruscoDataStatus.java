package cz.auderis.corusco.core.data;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Current data-source status.
 *
 * <p>Status is the scalar lifecycle companion to the current row window. It
 * lets a presenter distinguish "loading a newer page", "ready", "failed while
 * retaining old rows", and "closed" without encoding those states in the row
 * list itself. The refresh generation identifies the request that produced
 * the status and can be displayed or logged for diagnostics.</p>
 *
 * <p>Only {@link CoruscoDataPhase#FAILED} may carry an error. Other phases
 * deliberately keep {@code error} null so stale failures are not accidentally
 * presented as current failure state after a later success, cancellation, or
 * close.</p>
 *
 * @param phase lifecycle phase
 * @param refreshGeneration current generation
 * @param error latest error, or {@code null}
 */
public record CoruscoDataStatus(CoruscoDataPhase phase, long refreshGeneration, @Nullable CoruscoDataError error) {

    /**
     * Initial idle status.
     *
     * <p>The initial status belongs to a source that has not yet requested a
     * page. It uses generation zero, matching the initial value of
     * {@link cz.auderis.corusco.core.task.GenerationCounter}.</p>
     */
    public static final CoruscoDataStatus INITIAL = new CoruscoDataStatus(CoruscoDataPhase.IDLE, 0L, null);

    /**
     * Creates status.
     *
     * <p>The generation must be non-negative. Supplying an error for a
     * non-failed phase is rejected because it would make the public status
     * ambiguous.</p>
     *
     * @param phase lifecycle phase
     * @param refreshGeneration generation
     * @param error latest error
     */
    public CoruscoDataStatus {
        Objects.requireNonNull(phase, "phase");
        if (refreshGeneration < 0L) {
            throw new IllegalArgumentException("refreshGeneration must not be negative");
        }
        if (phase != CoruscoDataPhase.FAILED && error != null) {
            throw new IllegalArgumentException("only failed status may carry an error");
        }
    }
}
