package cz.auderis.corusco.core.lifecycle;

/**
 * Reports one or more failures that occurred while detaching a
 * {@link DetachableScope}.
 *
 * <p>The scope continues detaching every child before throwing this exception.
 * Individual failures are available through {@link #getSuppressed()}.</p>
 */
public final class DetachmentException extends RuntimeException {

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
