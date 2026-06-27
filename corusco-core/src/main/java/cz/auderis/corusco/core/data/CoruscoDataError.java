package cz.auderis.corusco.core.data;

import java.util.Objects;

/**
 * Failure recorded by a data source.
 *
 * <p>This record is the status-level failure value for a data source. It keeps
 * a concise message for presentation or logging and the original throwable for
 * diagnostics. It is not a replacement for structured validation problems;
 * row or form validation should still use
 * {@link cz.auderis.corusco.core.problem.ProblemSet} where appropriate.</p>
 *
 * @param message human-readable failure summary
 * @param cause original failure
 */
public record CoruscoDataError(String message, Throwable cause) {

    /**
     * Creates an error.
     *
     * <p>The message should be suitable as a short failure summary. Callers
     * that need localization can translate this record at the presentation
     * boundary or construct it with an already localized message.</p>
     *
     * @param message summary
     * @param cause cause
     */
    public CoruscoDataError {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(cause, "cause");
    }

    /**
     * Creates an error from a throwable.
     *
     * <p>If the throwable has a blank or missing message, the throwable's
     * simple class name is used as the summary. The original throwable remains
     * available through {@link #cause()}.</p>
     *
     * @param cause cause
     * @return data error
     */
    public static CoruscoDataError of(Throwable cause) {
        Objects.requireNonNull(cause, "cause");
        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            message = cause.getClass().getSimpleName();
        }
        return new CoruscoDataError(message, cause);
    }
}
