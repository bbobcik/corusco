package cz.auderis.corusco.core.data;

/**
 * Data-source lifecycle phase.
 *
 * <p>Phases describe the current request lifecycle of a
 * {@link CoruscoDataSource}. They do not describe row emptiness: a source can
 * be {@link #READY} with an empty page, or {@link #FAILED} while still exposing
 * rows from a previous successful page.</p>
 */
public enum CoruscoDataPhase {
    /**
     * No request is currently running and no successful page has been accepted
     * for the current source generation.
     */
    IDLE,
    /**
     * A request is running. Existing rows may still be visible until a newer
     * page succeeds.
     */
    LOADING,
    /**
     * A page has completed successfully and current rows/count reflect the
     * accepted result.
     */
    READY,
    /**
     * The latest current request failed. Failure details are available through
     * {@link CoruscoDataStatus#error()}.
     */
    FAILED,
    /**
     * The source has been closed and rejects future requests.
     */
    CLOSED
}
