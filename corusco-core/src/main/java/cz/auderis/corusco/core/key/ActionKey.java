package cz.auderis.corusco.core.key;

/**
 * Stable identity for a command or action.
 *
 * <p>The id is intended for generated action descriptors, diagnostics, and UI
 * state boundaries. Equality and hash code are based on the id.</p>
 *
 * @param id stable non-blank action id
 */
public record ActionKey(String id) {

    /**
     * Creates an action key.
     *
     * @param id stable non-blank action id
     */
    public ActionKey {
        id = KeyIds.requireId(id);
    }

    /**
     * Creates an action key for hand-written tests and generated-style code.
     *
     * @param id stable non-blank action id
     * @return action key
     */
    public static ActionKey of(String id) {
        return new ActionKey(id);
    }

    @Override
    public String toString() {
        return "ActionKey[" + id + "]";
    }
}
