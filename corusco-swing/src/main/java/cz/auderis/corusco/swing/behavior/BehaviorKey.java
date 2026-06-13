package cz.auderis.corusco.swing.behavior;

import java.util.Objects;

/**
 * Stable identity for a behavior kind.
 *
 * <p>Keys are used for diagnostics, generated behavior plans, and conflict
 * checks. Equality and hash code are based on the stable id.</p>
 *
 * @param id stable non-blank behavior id
 */
public record BehaviorKey(String id) {

    /**
     * Creates a behavior key.
     *
     * @param id stable non-blank behavior id
     */
    public BehaviorKey {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }

    /**
     * Creates a behavior key.
     *
     * @param id stable non-blank behavior id
     * @return behavior key
     */
    public static BehaviorKey of(String id) {
        return new BehaviorKey(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
