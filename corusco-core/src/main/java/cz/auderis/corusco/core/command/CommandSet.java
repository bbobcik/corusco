package cz.auderis.corusco.core.command;

import cz.auderis.corusco.core.key.ActionKey;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable command registry keyed by stable {@link ActionKey} values.
 *
 * <p>A command set is a small presentation-model companion for a view or
 * presenter. It lets generated view plans, Swing test helpers, and menu/button
 * assembly code find commands by typed action key without relying on field
 * names or localized text. The set preserves insertion order for deterministic
 * menu and toolbar construction, but lookup is by key.</p>
 *
 * <p>The set owns no command lifecycle; it retains command references but does
 * not close or reset them. Duplicate keys are rejected at construction time so
 * a presenter cannot accidentally expose two different commands for one
 * action.</p>
 */
public final class CommandSet {

    private final Map<ActionKey, Command> commands;

    /**
     * Creates a command set.
     *
     * <p>The input collection is copied into an immutable insertion-ordered
     * registry. {@code null} commands and duplicate action keys are rejected.</p>
     *
     * @param commands commands to include; duplicate keys are rejected
     */
    public CommandSet(Collection<? extends Command> commands) {
        Objects.requireNonNull(commands, "commands");
        Map<ActionKey, Command> indexed = new LinkedHashMap<>();
        for (Command command : commands) {
            Objects.requireNonNull(command, "command");
            Command previous = indexed.putIfAbsent(command.key(), command);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate command key: " + command.key());
            }
        }
        this.commands = Collections.unmodifiableMap(new LinkedHashMap<>(indexed));
    }

    /**
     * Creates a command set from commands.
     *
     * @param commands commands to include
     * @return command set
     */
    public static CommandSet of(Command... commands) {
        return new CommandSet(List.of(commands));
    }

    /**
     * Looks up a command.
     *
     * @param key action key
     * @return command if present
     */
    public Optional<Command> find(ActionKey key) {
        return Optional.ofNullable(commands.get(Objects.requireNonNull(key, "key")));
    }

    /**
     * Requires a command to be present.
     *
     * @param key action key
     * @return command
     */
    public Command require(ActionKey key) {
        return find(key).orElseThrow(() -> new IllegalArgumentException("Unknown command key: " + key));
    }

    /**
     * Returns commands in insertion order.
     *
     * @return immutable command collection
     */
    public Collection<Command> commands() {
        return commands.values();
    }
}
