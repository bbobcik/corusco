package cz.auderis.corusco.core.command;

import cz.auderis.corusco.core.value.ChangeOrigin;

/**
 * Command with mutable presentation state.
 *
 * <p>Generated presenters and hand-written controllers can expose this subtype
 * when they intentionally own enabled or selected state. UI adapters mutate
 * selected state only for selectable commands and use {@link ChangeOrigin#USER}
 * for user-originated toggle changes.</p>
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
