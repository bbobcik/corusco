package cz.auderis.corusco.core.lifecycle;

import java.io.Serial;

/**
 * Reports one or more failures that occurred while detaching a
 * {@link DetachableScope}.
 *
 * <p>The scope continues detaching every child before throwing this exception.
 * Individual failures are available through {@link #getSuppressed()}, in the
 * order the scope encountered them during cleanup. The exception therefore
 * represents a lifecycle cleanup failure after best-effort detachment, not a
 * signal that cleanup stopped at the first failing child.</p>
 */
public class DetachmentException extends ScopeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a detachment exception.
     *
     * @param message detail message
     */
    public DetachmentException(String message) {
        super(message);
    }
}
