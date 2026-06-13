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
 * Deterministic collection of commands keyed by {@link ActionKey}.
 */
public final class CommandSet {

    private final Map<ActionKey, Command> commands;

    /**
     * Creates a command set.
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
