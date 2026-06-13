package cz.auderis.corusco.core.lifecycle;

/**
 * Reports one or more failures that occurred while closing a
 * {@link SubscriptionScope}.
 *
 * <p>The scope continues closing every child before throwing this exception.
 * Individual cleanup failures are available through
 * {@link #getSuppressed()}.</p>
 */
public final class SubscriptionScopeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a scope cleanup exception.
     *
     * @param message detail message
     */
    public SubscriptionScopeException(String message) {
        super(message);
    }
}
