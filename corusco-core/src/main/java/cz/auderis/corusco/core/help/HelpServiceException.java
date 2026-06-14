package cz.auderis.corusco.core.help;

/**
 * Runtime exception for help-service configuration or dispatch failures.
 *
 * <p>{@link HelpService} implementations use this exception when a help topic
 * cannot be opened because required infrastructure is missing, misconfigured,
 * or rejects the request. It is separate from ordinary "no help topic is
 * configured" decisions, which should be represented by absent metadata before
 * a help request is made.</p>
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
