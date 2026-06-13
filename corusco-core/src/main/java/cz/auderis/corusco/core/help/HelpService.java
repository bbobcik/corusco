package cz.auderis.corusco.core.help;

import cz.auderis.corusco.core.key.HelpTopic;
import java.util.Optional;

/**
 * Service for opening help topics.
 *
 * <p>The service accepts stable generated {@link HelpTopic} descriptors and
 * dispatches immutable {@link HelpRequest} instances to an application-owned
 * {@link HelpHandler}. It is intentionally UI-toolkit neutral; Swing behavior
 * can adapt focus, F1 key events, and components to requests later.</p>
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
