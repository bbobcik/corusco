package cz.auderis.corusco.swing.behavior;

/**
 * Signals that a behavior cannot be installed into a {@link BehaviorScope}.
 *
 * <p>The scope throws this exception before invoking the conflicting behavior
 * installation when descriptors violate cardinality or primary-binding rules.
 * The exception identifies configuration errors in a generated or handwritten
 * view plan; it is not used for failures that occur while a behavior is
 * touching Swing components.</p>
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
