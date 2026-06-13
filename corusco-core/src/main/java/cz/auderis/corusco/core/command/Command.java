package cz.auderis.corusco.core.command;

import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.value.ReadableValue;

/**
 * Type-safe command that can back multiple UI controls.
 *
 * <p>A command owns its identity, presentation metadata, enabled state, optional
 * selected state for toggle actions, and invocation handler. Swing buttons,
 * menu items, toolbar buttons, and key bindings should adapt the same command
 * instance instead of installing separate handlers.</p>
 */
public interface Command {

    /**
     * Returns the stable command key.
     *
     * @return action key
     */
    ActionKey key();

    /**
     * Returns presentation metadata.
     *
     * @return descriptor
     */
    ActionDescriptor descriptor();

    /**
     * Returns observable enabled state.
     *
     * @return enabled value
     */
    ReadableValue<Boolean> enabled();

    /**
     * Returns observable selected state.
     *
     * <p>For non-selectable commands this value is always {@code false}.</p>
     *
     * @return selected value
     */
    ReadableValue<Boolean> selected();

    /**
     * Indicates whether this command owns toggle selected state.
     *
     * @return selectable flag
     */
    default boolean selectable() {
        return descriptor().selectable();
    }

    /**
     * Returns the current enabled state.
     *
     * @return enabled flag
     */
    default boolean isEnabled() {
        return Boolean.TRUE.equals(enabled().value());
    }

    /**
     * Returns the current selected state.
     *
     * @return selected flag
     */
    default boolean isSelected() {
        return Boolean.TRUE.equals(selected().value());
    }

    /**
     * Runs the command handler.
     */
    void execute();
}
