package cz.auderis.corusco.core.help;

/**
 * Handles help open requests.
 *
 * <p>Applications can implement this interface with a browser launcher,
 * embedded viewer, dialog, or test recorder. Handlers should be deterministic
 * about threading at their own boundary; the core API imposes no Swing
 * dependency.</p>
 */
@FunctionalInterface
public interface HelpHandler {

    /**
     * Opens help for a request.
     *
     * @param request help request
     */
    void openHelp(HelpRequest request);
}
