/**
 * Reusable Swing component behaviors installed through deterministic scopes.
 *
 * <p>This package solves the problem of assembling a Swing view from many small
 * component capabilities without hiding those capabilities in ad hoc listener
 * code. A text field often needs a model binding, validation tooltip,
 * validation border, select-all-on-focus behavior, accessible text, and maybe
 * F1 help. A button may need command binding and keyboard metadata. Installing
 * all of that directly in a presenter quickly becomes hard to audit and hard
 * to dispose. Behavior objects make those capabilities explicit, ordered, and
 * lifecycle-owned.</p>
 *
 * <p>Use this package when component setup should be repeatable, testable, or
 * generated. Generated forms use it for standard field behavior plans.
 * Handwritten views use it when several components need the same lifecycle
 * rules or when a presenter should describe <em>what</em> a component does
 * without embedding all listener mechanics inline. If you only need one direct
 * component/model connection, the lower-level
 * {@link cz.auderis.corusco.swing.binding} package may be enough.</p>
 *
 * <p>The central design idea is that behavior installation is a plan, not an
 * accident of constructor code. A behavior declares its key, phase, and
 * cardinality before it touches a component. That lets a generated plan and a
 * handwritten extension cooperate without silently installing two primary
 * bindings, two tooltip owners, or conflicting keyboard actions on the same
 * component.</p>
 *
 * <p>The first step is to create a {@link
 * cz.auderis.corusco.swing.behavior.BehaviorScope} for one view or dialog
 * activation. Then install behaviors on components:</p>
 *
 * <pre>{@code
 * BehaviorScope scope = new BehaviorScope();
 * scope.install(nameField, List.of(
 *         StandardBehaviors.textFieldBinding(model.name),
 *         StandardBehaviors.validationTooltip(model.name.problemSet()),
 *         StandardBehaviors.validationBorder(model.name.problemSet()),
 *         StandardBehaviors.selectAllOnFocus()
 * ));
 * }</pre>
 *
 * <p>Close the scope when the view is disposed. The scope closes installed
 * bindings in one place, which removes listeners, subscriptions, input-map
 * entries, action-map entries, borders, and other component state owned by the
 * installed behaviors. The scope is not a global registry; create one per
 * active view, dialog, or screen fragment.</p>
 *
 * <p>For generated forms, the first step is even shorter: implement the
 * generated view interface, construct the generated form model, create a
 * {@code BehaviorScope}, and call the generated bindings facade. A typical
 * panel then adds any application-specific behaviors, such as help, status
 * text, or command-specific keyboard shortcuts, to the same scope.</p>
 *
 * <p>Start with {@link cz.auderis.corusco.swing.behavior.StandardBehaviors} for
 * built-in form behaviors: text and checkbox bindings, validation feedback,
 * composed tooltips, accessible text, status text, help-on-F1, and busy
 * overlays. Use {@link cz.auderis.corusco.swing.behavior.CommandBehaviors} to
 * adapt core commands to buttons, menu items, and key bindings. Write a custom
 * {@link cz.auderis.corusco.swing.behavior.ViewBehavior} only when the standard
 * factories cannot express the component capability.</p>
 *
 * <p>When writing a custom behavior, keep the behavior small and reversible.
 * The {@code install} method should add exactly the listeners, component
 * properties, action-map entries, or subscriptions needed for one capability,
 * and the returned binding should undo those changes. If a behavior needs
 * services such as help dispatch, get them from the {@code BehaviorContext}
 * rather than from a global singleton.</p>
 *
 * <p>Behaviors are described by {@link
 * cz.auderis.corusco.swing.behavior.BehaviorDescriptor}, {@link
 * cz.auderis.corusco.swing.behavior.BehaviorKey}, {@link
 * cz.auderis.corusco.swing.behavior.BehaviorPhase}, and {@link
 * cz.auderis.corusco.swing.behavior.BehaviorCardinality}. Descriptors let
 * generated and handwritten plans detect conflicts such as two primary text
 * bindings for the same component. {@link
 * cz.auderis.corusco.swing.behavior.BindingBehavior} marks primary
 * model/component bindings; {@link
 * cz.auderis.corusco.swing.behavior.DecorationBehavior} marks secondary UI
 * state such as tooltips, borders, accessible text, or busy overlays.</p>
 *
 * <p>Generated {@code @SwingForm} sources produce form-specific companions,
 * such as {@code CustomerEditBehaviorPlan} and {@code CustomerEditBindings},
 * that install these behavior factories against the generated view contract.
 * For example, a source record named {@code CustomerEdit} annotated with
 * {@code @SwingForm} produces companions whose names start with
 * {@code CustomerEdit}, including {@code CustomerEditBehaviorPlan} and
 * {@code CustomerEditBindings}.
 * For generated forms, call the generated bindings facade first, then add any
 * application specific behaviors to the same scope. For handwritten forms,
 * assemble the behavior lists directly in view construction or presenter
 * activation code.</p>
 *
 * <p>Testing usually inspects effects rather than behavior objects. Use
 * {@link cz.auderis.corusco.swing.behavior.BehaviorScope#installedBehaviorKeys(javax.swing.JComponent)}
 * when a test must verify that a generated or handwritten plan installed the
 * expected capabilities. Use component-level assertions for text, borders,
 * tooltips, action-map entries, or command state when the actual UI effect is
 * what matters.</p>
 *
 * <p>Behavior installation touches Swing components and should run on the Event
 * Dispatch Thread. Keep model mutation, component creation, behavior
 * installation, and scope cleanup within the same EDT lifecycle unless a
 * specific binding documents a dispatcher boundary.</p>
 *
 * <p>Power users should avoid turning behaviors into a dependency-injection
 * framework. The package is intentionally about view capabilities and
 * lifecycle ownership. Presenter decisions, service calls, cross-field
 * validation, and business workflows should stay in presenter or model code,
 * with behaviors acting as the Swing boundary.</p>
 */
package cz.auderis.corusco.swing.behavior;
