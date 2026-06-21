package cz.auderis.corusco.core.lifecycle;

import java.io.Serial;

/**
 * Reports one or more failures that occurred while closing a
 * {@link SubscriptionScope}.
 *
 * <p>The scope continues closing every child before throwing this exception.
 * Individual cleanup failures are available through
 * {@link #getSuppressed()}.</p>
 */
public class ScopeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a scope cleanup exception.
     *
     * @param message detail message
     */
    public ScopeException(String message) {
        super(message);
    }

}
