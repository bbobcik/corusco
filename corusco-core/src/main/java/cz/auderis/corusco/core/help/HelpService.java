package cz.auderis.corusco.core.help;

import cz.auderis.corusco.core.key.HelpTopic;
import java.util.Optional;

/**
 * Service for opening help topics.
 *
 * <p>The service accepts stable generated {@link HelpTopic} descriptors and
 * opens the corresponding help content through application infrastructure.
 * Implementations may delegate to a browser, embedded help panel, platform help
 * system, telemetry recorder, or test double. The service is intentionally
 * UI-toolkit neutral; Swing behavior adapts focus, F1 key events, and
 * components to {@link HelpRequest} instances at the boundary.</p>
 *
 * <p>Callers should only invoke the service when metadata says help is
 * available. Missing-topic decisions belong to descriptors, tooltip policy, or
 * behavior installation. Implementors should throw {@link HelpServiceException}
 * for configuration or dispatch failures that occur after a request is made.
 * {@link #lastRequest()} is primarily a diagnostic and test-support hook.</p>
 */
public interface HelpService {

    /**
     * Opens help for a topic.
     *
     * @param topic help topic
     */
    default void open(HelpTopic topic) {
        open(new HelpRequest(topic, null, null));
    }

    /**
     * Opens help for a topic with source and context metadata.
     *
     * @param topic help topic
     * @param source optional source
     * @param context optional context
     */
    default void open(HelpTopic topic, Object source, String context) {
        open(new HelpRequest(topic, source, context));
    }

    /**
     * Opens help for a request.
     *
     * @param request help request
     */
    void open(HelpRequest request);

    /**
     * Returns the most recent request seen by the service.
     *
     * @return last request
     */
    Optional<HelpRequest> lastRequest();
}
