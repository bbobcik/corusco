/**
 * Toolkit-neutral command descriptors, command state, and executable actions.
 *
 * <p>This package models user actions before they are attached to buttons,
 * menu items, key bindings, or generated behavior plans. Start with
 * {@link cz.auderis.corusco.core.command.Command}: it combines a stable
 * {@link cz.auderis.corusco.core.key.ActionKey}, an
 * {@link cz.auderis.corusco.core.command.ActionDescriptor}, executable
 * behavior, and observable enabled state. A command is not a Swing action; it
 * is the model object that Swing actions adapt.</p>
 *
 * <p>{@link cz.auderis.corusco.core.command.ActionDescriptor} contains the
 * presentation metadata that generated code and UI adapters need, such as text
 * resource keys, descriptions, and optional
 * {@link cz.auderis.corusco.core.command.AcceleratorDescriptor} values.
 * {@link cz.auderis.corusco.core.command.CommandFactory} creates common command
 * implementations. {@link cz.auderis.corusco.core.command.MutableCommand}
 * supports commands whose enabled state changes as form or selection state
 * changes. {@link cz.auderis.corusco.core.command.CommandSet} groups commands
 * by key for presenters and generated view plans.</p>
 *
 * <p>{@link cz.auderis.corusco.core.command.CommandHandler} is where
 * application-specific side effects happen. Core command execution is
 * synchronous and runs on the thread that calls
 * {@link cz.auderis.corusco.core.command.Command#execute()}. When the same
 * command is invoked through Swing, that thread is normally the Event Dispatch
 * Thread, so handlers that do expensive work should hand it to a task service
 * instead of blocking the UI.</p>
 *
 * <p>Swing adapters live in {@code cz.auderis.corusco.swing.command} and
 * command behavior factories live in {@code cz.auderis.corusco.swing.behavior}.
 * Those packages resolve resources, create {@code javax.swing.Action}
 * instances, install key bindings, and clean up listeners. This package remains
 * responsible only for command identity, metadata, state, and invocation.</p>
 */
package cz.auderis.corusco.core.command;
