package cz.auderis.corusco.core.resource;

/**
 * Runtime exception for resource lookup failures.
 *
 * <p>Missing required resources and values whose runtime type does not match
 * the requested key type both use this exception so callers can distinguish
 * resource configuration failures from ordinary optional misses. Optional
 * lookup methods return {@code Optional.empty()} for absent keys instead of
 * throwing this exception.</p>
 */
public class ResourceException extends RuntimeException {

    /**
     * Creates an exception with a message.
     *
     * @param message failure message
     */
    public ResourceException(String message) {
        super(message);
    }
}
