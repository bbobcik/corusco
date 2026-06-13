package cz.auderis.corusco.core.command;

/**
 * Handler invoked when a command executes.
 */
@FunctionalInterface
public interface CommandHandler {

    /**
     * Executes command-specific behavior.
     *
     * @param command command being invoked
     */
    void execute(Command command);
}
