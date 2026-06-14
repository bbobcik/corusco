package cz.auderis.corusco.core.form;

/**
 * Signals that a {@link FormModel} cannot produce a result in its current
 * state.
 *
 * <p>The exception is used for failed commit attempts, not for ordinary field
 * validation reporting. Callers should normally inspect
 * {@link FormModel#problems()} or {@link FormModel#isCommittable()} before
 * asking for a result; this exception protects the model contract when that
 * precondition is violated or becomes false during commit.</p>
 */
public final class UncommittableFormException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an uncommittable form exception.
     *
     * @param message detail message
     */
    public UncommittableFormException(String message) {
        super(message);
    }
}
