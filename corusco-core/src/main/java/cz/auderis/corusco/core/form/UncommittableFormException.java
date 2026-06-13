package cz.auderis.corusco.core.form;

/**
 * Thrown when a form result is requested while the form has blocking problems.
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
