/**
 * Disposable Swing bindings between components and Corusco models.
 *
 * <p>This package is the direct component/model plumbing layer. Start with
 * {@link cz.auderis.corusco.swing.binding.Binding}, the lifecycle handle
 * returned after a binding installs Swing listeners, model subscriptions, or
 * replaced component state. {@link cz.auderis.corusco.swing.binding.BindingFactory}
 * contains the common bindings for text fields, text areas, selected-state
 * buttons, validation feedback, tooltips, status text, and accessible text.</p>
 *
 * <p>{@link cz.auderis.corusco.swing.binding.BindingScope} groups multiple
 * bindings under one owner so a presenter, dialog, or behavior scope can close
 * them together. {@link cz.auderis.corusco.swing.binding.SwingEdt} centralizes
 * EDT checks and dispatch helpers. {@link
 * cz.auderis.corusco.swing.binding.SwingEditors} contains small editor-related
 * helpers used by dialog and binding code.</p>
 *
 * <p>Bindings are explicit about ownership: they retain the component, model,
 * listeners, and subscriptions they install until closed. They generally touch
 * Swing components during installation, model-to-component updates, and
 * cleanup, so use them on the Event Dispatch Thread unless a specific type says
 * otherwise. Closing a binding should be part of the view lifecycle, not left
 * to garbage collection.</p>
 *
 * <p>Higher-level behavior installation lives in
 * {@code cz.auderis.corusco.swing.behavior}. Collection and table-specific
 * adapters live in neighboring {@code collection} and {@code table} packages.
 * This package is the place to read when you need to understand the concrete
 * listener/subscription relationship between a Swing component and a core
 * model.</p>
 */
package cz.auderis.corusco.swing.binding;
