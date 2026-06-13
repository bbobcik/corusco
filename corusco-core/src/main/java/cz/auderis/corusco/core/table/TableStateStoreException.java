package cz.auderis.corusco.core.table;

/**
 * Runtime exception used for table state store read/write failures.
 *
 * <p>The exception boundary keeps the public store API compact while still
 * distinguishing persistence failures from descriptor merge validation errors.
 * Store implementations should include the table id or operation in the
 * message when that context is available.</p>
 */
public class TableStateStoreException extends RuntimeException {

    /**
     * Creates an exception with a message.
     *
     * @param message failure message
     */
    public TableStateStoreException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and cause.
     *
     * @param message failure message
     * @param cause underlying failure
     */
    public TableStateStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
