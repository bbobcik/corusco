package cz.auderis.corusco.core.task;

/**
 * Signals cooperative task cancellation.
 */
public final class TaskCancelledException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a cancellation exception.
     *
     * @param message detail message
     */
    public TaskCancelledException(String message) {
        super(message);
    }
}
