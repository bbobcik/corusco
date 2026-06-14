package cz.auderis.corusco.core.command;

/**
 * Callback that performs a command's application-specific action.
 *
 * <p>Handlers are invoked synchronously by the command implementation. Core
 * commands are Swing-free, so the thread is whichever thread calls
 * {@link Command#execute()}. When commands are triggered through Swing
 * adapters, handlers normally run on the Event Dispatch Thread and should keep
 * UI responsiveness in mind.</p>
 *
 * <p>Implementation contract: handlers should not mutate the command's key or
 * descriptor; those values identify the command for generated metadata and UI
 * bindings.</p>
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
