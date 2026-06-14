package cz.auderis.corusco.core.help;

import java.util.Objects;
import java.util.Optional;

/**
 * Simple {@link HelpService} implementation backed by a replaceable {@link HelpHandler}.
 *
 * <p>This service is useful for tests, examples, and small applications that
 * want the core help contract without a larger dependency-injection boundary.
 * It records the most recent {@link HelpRequest} for diagnostics, then
 * delegates the actual help action to the configured handler. A handler might
 * open a browser, show an embedded help pane, invoke platform help, or simply
 * record the request in a test.</p>
 *
 * <p>The service owns neither the source object in a request nor any UI opened
 * by the handler. It is mutable because the handler can be replaced, and it is
 * not synchronized; callers should confine it to their application UI/service
 * thread or provide external coordination. Calling {@link #open(HelpRequest)}
 * without a configured handler records the request and then throws
 * {@link HelpServiceException}.</p>
 */
public final class DefaultHelpService implements HelpService {

    private HelpHandler handler;
    private HelpRequest lastRequest;

    /**
     * Creates a service with no handler.
     *
     * <p>A handler must be supplied with {@link #setHandler(HelpHandler)}
     * before help requests can succeed.</p>
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
     * <p>The previous handler is not closed or otherwise notified. This service
     * only keeps a reference to the currently configured handler.</p>
     *
     * @param handler help handler
     */
    public void setHandler(HelpHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    /**
     * Records and dispatches a help request.
     *
     * @param request help request to open
     * @throws HelpServiceException if no handler is configured
     */
    @Override
    public void open(HelpRequest request) {
        Objects.requireNonNull(request, "request");
        lastRequest = request;
        if (handler == null) {
            throw new HelpServiceException("No help handler configured for topic: " + request.topic().id());
        }
        handler.openHelp(request);
    }

    /**
     * Returns the most recent request passed to {@link #open(HelpRequest)}.
     *
     * @return last request, or empty before the first request
     */
    @Override
    public Optional<HelpRequest> lastRequest() {
        return Optional.ofNullable(lastRequest);
    }
}
