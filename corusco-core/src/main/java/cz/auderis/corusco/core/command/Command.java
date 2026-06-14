package cz.auderis.corusco.core.command;

import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.value.ReadableValue;

/**
 * Type-safe command that can back multiple UI controls.
 *
 * <p>A command owns its identity, presentation metadata, enabled state, optional
 * selected state for toggle actions, and invocation behavior. It is the
 * toolkit-neutral action model used by presenters and generated metadata.
 * Swing buttons, menu items, toolbar buttons, and key bindings should adapt the
 * same command instance instead of installing separate handlers with duplicated
 * enabled-state logic.</p>
 *
 * <p>Execution is synchronous and occurs on the thread that calls
 * {@link #execute()}. The core command contract does not start background work
 * or marshal to Swing. When a command is triggered through Swing adapters,
 * handlers normally run on the Event Dispatch Thread and should delegate slow
 * work to a task service.</p>
 *
 * <p>Implementations must keep {@link #key()} consistent with
 * {@link #descriptor()}; adapters and command sets rely on that stable
 * identity. Non-selectable commands should expose a selected value that remains
 * {@code false}.</p>
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
     * Runs the command handler when the command is enabled.
     *
     * <p>Implementations may ignore execution while disabled, as the standard
     * command implementation does. Callers that need to report disabled-command
     * attempts should check {@link #isEnabled()} before calling this method.</p>
     */
    void execute();
}
