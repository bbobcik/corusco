/**
 * Reusable Swing component behaviors installed through deterministic scopes.
 *
 * <p>This package is for code that wants to describe component capabilities
 * separately from the component construction code. Start with
 * {@link cz.auderis.corusco.swing.behavior.ViewBehavior}: a behavior receives a
 * {@link cz.auderis.corusco.swing.behavior.BehaviorContext}, installs Swing
 * listeners or component state, and returns a disposable
 * {@link cz.auderis.corusco.swing.binding.Binding}. {@link
 * cz.auderis.corusco.swing.behavior.BehaviorScope} owns installed behaviors and
 * closes them in a predictable lifecycle.</p>
 *
 * <p>Behaviors are described by {@link
 * cz.auderis.corusco.swing.behavior.BehaviorDescriptor}, {@link
 * cz.auderis.corusco.swing.behavior.BehaviorKey}, {@link
 * cz.auderis.corusco.swing.behavior.BehaviorPhase}, and {@link
 * cz.auderis.corusco.swing.behavior.BehaviorCardinality}. These descriptors
 * let generated and handwritten view plans detect conflicts such as two primary
 * text bindings for the same component. {@link
 * cz.auderis.corusco.swing.behavior.BindingBehavior} marks primary
 * model/component bindings; {@link
 * cz.auderis.corusco.swing.behavior.DecorationBehavior} marks secondary UI
 * state such as tooltips, borders, accessible text, or busy overlays.</p>
 *
 * <p>{@link cz.auderis.corusco.swing.behavior.StandardBehaviors} contains
 * built-in behavior factories for common bindings and decorations. {@link
 * cz.auderis.corusco.swing.behavior.CommandBehaviors} adapts core commands to
 * buttons, menu items, and key bindings. {@link
 * cz.auderis.corusco.swing.behavior.StandardBehaviorKeys} contains shared keys
 * used by generated and handwritten plans.</p>
 *
 * <p>Behavior installation touches Swing components and should run on the Event
 * Dispatch Thread. The returned bindings own installed listeners, action-map
 * entries, input-map entries, component text, borders, or other modified state
 * according to the specific behavior. Close the owning scope when the view or
 * dialog is disposed.</p>
 */
package cz.auderis.corusco.swing.behavior;
