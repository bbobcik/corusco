package cz.auderis.corusco.core.key;

/**
 * Stable identity for a help topic.
 *
 * <p>The id is a boundary string for generated help descriptors and help-system
 * lookup. Equality and hash code are based on the id.</p>
 *
 * <p>Generated form-field and table-column descriptors create help-topic
 * values from {@code @Help(topic = ...)} metadata. The values are embedded in
 * generated descriptors rather than exposed as standalone constants. Use
 * {@link #of(String)} for handwritten descriptors, tests, and application help
 * routing.</p>
 *
 * @param id stable non-blank help topic id
 */
public record HelpTopic(String id) {

    /**
     * Creates a help topic key.
     *
     * @param id stable non-blank help topic id
     */
    public HelpTopic {
        id = KeyIds.requireId(id);
    }

    /**
     * Creates a help topic for hand-written tests and generated-style code.
     *
     * @param id stable non-blank help topic id
     * @return help topic key
     */
    public static HelpTopic of(String id) {
        return new HelpTopic(id);
    }

    @Override
    public String toString() {
        return "HelpTopic[" + id + "]";
    }
}
