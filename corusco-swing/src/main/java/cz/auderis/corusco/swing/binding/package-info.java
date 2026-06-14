/**
 * Disposable Swing bindings between components and Corusco models.
 *
 * <p>This package solves the direct synchronization problem between Swing
 * components and toolkit-neutral Corusco state. Swing components expose
 * document events, item events, action maps, borders, tooltips, focus state,
 * and editor state. Core models expose observable values, field models,
 * commands, and problem sets. Bindings are the small lifecycle objects that
 * connect those two worlds and then undo their work when the view closes.</p>
 *
 * <p>Use this package when you are wiring a concrete Swing component to a
 * concrete model by hand, writing a custom behavior, or debugging what a
 * generated behavior ultimately installs. For common generated or repeated view
 * setup, prefer {@code cz.auderis.corusco.swing.behavior}; behavior factories
 * usually delegate to these bindings internally.</p>
 *
 * <p>The first step is to install one binding and keep the returned {@link
 * cz.auderis.corusco.swing.binding.Binding}:</p>
 *
 * <pre>{@code
 * Binding binding = BindingFactory.textField(nameField, model.name);
 * ...
 * binding.close();
 * }</pre>
 *
 * <p>Real views usually have many bindings. Put them in a {@link
 * cz.auderis.corusco.swing.binding.BindingScope} owned by the view, presenter,
 * dialog lifecycle, or behavior scope:</p>
 *
 * <pre>{@code
 * BindingScope scope = new BindingScope();
 * scope.add(BindingFactory.textField(nameField, model.name));
 * scope.add(BindingFactory.validationTooltip(nameField, model.name.problemSet()));
 * scope.close();
 * }</pre>
 *
 * <p>{@link cz.auderis.corusco.swing.binding.BindingFactory} contains the
 * common bindings for text fields, text areas, selected-state buttons,
 * validation feedback, composed tooltips, status text, and accessible text.
 * {@link cz.auderis.corusco.swing.binding.SwingEditors} contains editor-related
 * helpers used by dialogs, especially committing active table or formatted-text
 * editors before reading a result. {@link
 * cz.auderis.corusco.swing.binding.SwingEdt} centralizes Event Dispatch Thread
 * checks and dispatch helpers.</p>
 *
 * <p>Bindings are explicit about ownership. They retain the component, model,
 * listeners, subscriptions, and previous component state they install until
 * closed. Closing a binding is not optional cleanup; it is how views avoid
 * duplicate listeners after reopen, stale model subscriptions, and component
 * state leaking from one dialog activation to the next.</p>
 *
 * <p>Bindings generally touch Swing components during installation,
 * model-to-component updates, user-event handling, and cleanup. Install and
 * close them on the Event Dispatch Thread unless a specific type documents a
 * different dispatcher. If the source model changes off the EDT, insert an
 * explicit dispatch boundary before connecting it to Swing.</p>
 *
 * <p>Initialization order matters. Build the component, initialize static Swing
 * properties such as columns or renderers, create the model, then install
 * bindings. After a binding is installed, the binding owns synchronization for
 * the properties it manages; later direct component writes can fight with
 * model-to-component updates unless they are part of the binding contract.</p>
 *
 * <p>User-originated updates should preserve origin information when the model
 * supports it. Text and selected-state bindings use
 * {@code cz.auderis.corusco.core.value.ChangeOrigin} so presenters can
 * distinguish user edits from programmatic reset, load, or apply operations.
 * This distinction is useful for dirty tracking, validation timing, and
 * avoiding feedback loops.</p>
 *
 * <p>Practical patterns are: create models before components are bound; install
 * bindings after components are constructed and initialized; keep the returned
 * binding or add it to a scope immediately; close the scope when the owning
 * view closes; and use behavior scopes when a binding is part of a reusable
 * component capability rather than one-off wiring.</p>
 *
 * <p>Testing bindings is usually easiest with an EDT-aware test harness:
 * construct the component and model, install the binding, perform a user-like
 * component change, assert the model, perform a model change, assert the
 * component, then close the binding and verify later changes no longer
 * propagate. Avoid relying on garbage collection to prove cleanup.</p>
 *
 * <p>Advanced bindings should be narrow. A binding that owns a document
 * listener should not also own unrelated border, tooltip, and keyboard state
 * unless those states are inseparable. Narrow bindings compose cleanly in
 * {@link cz.auderis.corusco.swing.binding.BindingScope} and are easier for
 * generated behaviors to reuse.</p>
 */
package cz.auderis.corusco.swing.binding;
