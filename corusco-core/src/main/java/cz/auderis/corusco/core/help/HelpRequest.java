package cz.auderis.corusco.core.help;

import cz.auderis.corusco.core.key.HelpTopic;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable request to open help for a topic.
 *
 * <p>The request is Swing-free. {@code source} may be a component, descriptor,
 * command, or other application object supplied by the UI layer. {@code
 * context} is optional caller-provided text for status messages or analytics;
 * it is not interpreted by the core service.</p>
 *
 * @param topic stable help topic
 * @param source optional request source
 * @param context optional context text
 */
public record HelpRequest(HelpTopic topic, Object source, String context) {

    /**
     * Creates a help request.
     *
     * @param topic stable help topic
     * @param source optional request source
     * @param context optional context text
     */
    public HelpRequest {
        Objects.requireNonNull(topic, "topic");
    }

    /**
     * Creates a request for a topic.
     *
     * @param topic stable help topic
     * @return help request
     */
    public static HelpRequest of(HelpTopic topic) {
        return new HelpRequest(topic, null, null);
    }

    /**
     * Returns the optional source.
     *
     * @return optional source
     */
    public Optional<Object> sourceOptional() {
        return Optional.ofNullable(source);
    }

    /**
     * Returns the optional context.
     *
     * @return optional context
     */
    public Optional<String> contextOptional() {
        return Optional.ofNullable(context);
    }
}
