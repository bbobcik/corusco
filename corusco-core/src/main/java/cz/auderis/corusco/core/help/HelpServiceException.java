package cz.auderis.corusco.core.help;

/**
 * Runtime exception for help-service configuration failures.
 */
public class HelpServiceException extends RuntimeException {

    /**
     * Creates an exception with a message.
     *
     * @param message failure message
     */
    public HelpServiceException(String message) {
        super(message);
    }
}
