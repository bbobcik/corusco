package cz.auderis.corusco.swing.behavior;

/**
 * Thrown when behavior installation violates cardinality or primary-binding
 * conflict rules.
 */
public final class BehaviorConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a behavior conflict exception.
     *
     * @param message detail message
     */
    public BehaviorConflictException(String message) {
        super(message);
    }
}
