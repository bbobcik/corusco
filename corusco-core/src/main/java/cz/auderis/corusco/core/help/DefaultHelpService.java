package cz.auderis.corusco.core.help;

import java.util.Objects;
import java.util.Optional;

/**
 * Default in-memory help service.
 *
 * <p>The service keeps the most recent request for diagnostics and tests, then
 * delegates opening to the configured {@link HelpHandler}. It is not
 * synchronized; applications should confine an instance to their UI/service
 * thread or provide external synchronization.</p>
 */
public final class DefaultHelpService implements HelpService {

    private HelpHandler handler;
    private HelpRequest lastRequest;

    /**
     * Creates a service with no handler.
     */
    public DefaultHelpService() {
    }

    /**
     * Creates a service with a handler.
     *
     * @param handler help handler
     */
    public DefaultHelpService(HelpHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    /**
     * Replaces the current help handler.
     *
     * @param handler help handler
     */
    public void setHandler(HelpHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public void open(HelpRequest request) {
        Objects.requireNonNull(request, "request");
        lastRequest = request;
        if (handler == null) {
            throw new HelpServiceException("No help handler configured for topic: " + request.topic().id());
        }
        handler.openHelp(request);
    }

    @Override
    public Optional<HelpRequest> lastRequest() {
        return Optional.ofNullable(lastRequest);
    }
}
