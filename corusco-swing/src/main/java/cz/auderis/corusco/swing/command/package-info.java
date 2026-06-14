/**
 * Swing adapters for Corusco command metadata and command state.
 *
 * <p>This package is the bridge from {@code cz.auderis.corusco.core.command}
 * to Swing actions. Start with
 * {@link cz.auderis.corusco.swing.command.SwingActionAdapter}, which wraps a
 * core {@link cz.auderis.corusco.core.command.Command} as a
 * {@code javax.swing.Action}. The adapter resolves presentation text through
 * {@link cz.auderis.corusco.swing.command.CommandResources}, observes enabled
 * state, invokes the command, and removes its subscriptions when closed.</p>
 *
 * <p>The package does not decide where an action is installed. Buttons, menu
 * items, and key maps are handled by behavior factories in
 * {@code cz.auderis.corusco.swing.behavior}. This separation lets generated
 * metadata define commands once and then install them in several Swing
 * locations without duplicating action state.</p>
 *
 * <p>Adapters touch Swing action properties and are intended for the Event
 * Dispatch Thread. Command handlers still run synchronously when the Swing
 * action fires. If the command performs slow work, hand that work to
 * {@code cz.auderis.corusco.core.task} or
 * {@code cz.auderis.corusco.swing.task} rather than blocking the EDT.</p>
 */
package cz.auderis.corusco.swing.command;
