package cz.auderis.corusco.core.command;

import cz.auderis.corusco.core.value.ChangeOrigin;

/**
 * Command with mutable presentation state.
 *
 * <p>Generated presenters and handwritten controllers expose this subtype when
 * they intentionally own enabled or selected state. The mutable state is still
 * observable through the base {@link Command} contract, so Swing adapters and
 * tests can subscribe without knowing who changes the state.</p>
 *
 * <p>Enabled-state changes usually come from model or validation state and use
 * {@link ChangeOrigin#MODEL}. UI adapters should mutate selected state only for
 * selectable commands and use {@link ChangeOrigin#USER} for user-originated
 * toggle changes. Implementations should reject selected-state mutation for
 * non-selectable commands rather than silently storing unsupported state.</p>
 */
public interface MutableCommand extends Command {

    /**
     * Sets enabled state with model origin.
     *
     * @param enabled enabled flag
     */
    default void setEnabled(boolean enabled) {
        setEnabled(enabled, ChangeOrigin.MODEL);
    }

    /**
     * Sets enabled state.
     *
     * @param enabled enabled flag
     * @param origin change origin
     */
    void setEnabled(boolean enabled, ChangeOrigin origin);

    /**
     * Sets selected state with model origin.
     *
     * @param selected selected flag
     */
    default void setSelected(boolean selected) {
        setSelected(selected, ChangeOrigin.MODEL);
    }

    /**
     * Sets selected state.
     *
     * @param selected selected flag
     * @param origin change origin
     */
    void setSelected(boolean selected, ChangeOrigin origin);
}
