/**
 * Swing integration layer for Corusco core models, generated support code, and
 * handwritten screens.
 *
 * <h2>What This Module Adds To Swing</h2>
 *
 * <p>This module connects toolkit-neutral Corusco models to concrete Swing
 * components. It does not replace Swing and does not choose your layout.
 * Applications still build panels, choose renderers, configure dialogs, and
 * decide screen flow. Corusco supplies the binding, lifecycle, command, table,
 * dialog, task, and testing support around those components.</p>
 *
 * <p>The central idea is that important screen state should not live only
 * inside Swing components. A text field can display raw text, but the form
 * model should own the raw text, parsed value, dirty state, and validation
 * problems. A button can display an action, but a command should own the
 * action identity, enabled state, and handler. A {@code JTable} can display
 * rows, but a table descriptor should own column identity and persistence
 * rules.</p>
 *
 * <p>This separation makes screens easier to test and refactor. Core tests can
 * exercise form models, commands, validators, and table descriptions without
 * opening a window. Swing tests can then focus on whether component bindings
 * install correctly, propagate changes, and clean up.</p>
 *
 * <h2>Why Plain Swing Wiring Becomes Fragile</h2>
 *
 * <p>Plain Swing encourages local listener code. A document listener updates a
 * model, another listener changes a button, an action object stores a label,
 * a table model relies on column index {@code 3}, and a tooltip is overwritten
 * by whichever feature installed last. This is easy to start and hard to
 * maintain.</p>
 *
 * <p>Corusco does not remove Swing's flexibility. Instead, it gives repeated
 * relationships a name and a lifecycle. A binding owns one component/model
 * connection. A behavior owns one reusable component capability. A binding
 * scope owns cleanup. A command adapter owns the connection between a core
 * command and a Swing {@code Action}. A table controller owns saved table
 * layout state.</p>
 *
 * <h2>Event Dispatch Thread Rule</h2>
 *
 * <p>Most types in this module are Event Dispatch Thread confined. Create
 * Swing components, install bindings, mutate bound component state, and close
 * Swing scopes on the EDT unless a specific type documents a different rule.
 * Core models are synchronous; the Swing boundary is where EDT discipline must
 * be applied.</p>
 *
 * <p>Background work should enter Swing through explicit boundaries, usually
 * in {@link cz.auderis.corusco.swing.task} or application dispatch code. Do
 * not rely on a core value or list to magically make a background mutation safe
 * for Swing.</p>
 *
 * <h2>Direct Bindings</h2>
 *
 * <p>{@link cz.auderis.corusco.swing.binding} is the first package to read
 * when one Swing component mirrors one core model. A binding installs Swing
 * listeners, subscribes to model changes, initializes component state, and
 * removes the installed listeners/subscriptions when closed.</p>
 *
 * <pre>{@code
 * BindingScope scope = new BindingScope();
 * scope.add(BindingFactory.textField(nameField, model.name));
 * scope.add(BindingFactory.validationTooltip(nameField, model.name.problemSet()));
 *
 * // Later, when the panel is no longer active:
 * scope.close();
 * }</pre>
 *
 * <p>Use direct bindings for local, concrete relationships: a
 * {@code JTextField} mirrors a text field model, a checkbox mirrors a boolean
 * field, a label mirrors a status value, or a tooltip mirrors validation
 * problems. Keep the returned binding or add it to a scope immediately.</p>
 *
 * <h2>Reusable Behaviors</h2>
 *
 * <p>{@link cz.auderis.corusco.swing.behavior} is a higher-level layer for
 * repeatable component capabilities. A behavior can mean "this component has
 * the primary text binding", "this component shows validation decoration",
 * "this component has F1 help", or "this component is connected to a command".
 * Generated behavior plans use this vocabulary.</p>
 *
 * <p>Behaviors help when several features touch the same component. Without a
 * plan, two helpers can both assume they own a tooltip, border, input map, or
 * document listener. Behavior descriptors and scopes provide names, ordering,
 * conflict detection, and cleanup.</p>
 *
 * <h2>Commands And Swing Actions</h2>
 *
 * <p>{@link cz.auderis.corusco.swing.command} adapts core commands to Swing
 * {@link javax.swing.Action} objects. The command remains the owner of
 * identity, enabled state, selected state, and invocation. The Swing action
 * becomes the component-facing adapter for buttons, menu items, toolbar items,
 * popup menus, and key bindings.</p>
 *
 * <pre>{@code
 * CommandSet commands = CustomerPresenterActions.commands(presenter);
 * SwingActionAdapter saveAction = new SwingActionAdapter(
 *         commands.require(CustomerPresenterActions.SAVE),
 *         resources::text
 * );
 * saveButton.setAction(saveAction);
 * }</pre>
 *
 * <p>This avoids duplicating action labels, tooltip text, keyboard shortcuts,
 * and enabled-state logic at every UI location where the same action appears.
 * The same command can drive a button and a menu item.</p>
 *
 * <h2>Lists And Combo Boxes</h2>
 *
 * <p>{@link cz.auderis.corusco.swing.collection} adapts core observable lists
 * to Swing list and combo-box models. Use it when a presenter owns a list of
 * values and Swing should display that list without copying it into an
 * unrelated model.</p>
 *
 * <p>The package also provides an EDT wrapper for observable lists. Use that
 * boundary when list changes originate outside the EDT but are consumed by
 * Swing components.</p>
 *
 * <h2>Tables</h2>
 *
 * <p>{@link cz.auderis.corusco.swing.table} adapts a core table descriptor and
 * an observable row list to a {@code JTable}. A table descriptor is the typed
 * description of the table: row type, columns, column ids, label resources,
 * read functions, edit functions, default widths, and saved-layout ids.</p>
 *
 * <p>This solves a frequent Swing table problem. Ordinary table models often
 * attach meaning to integer column indexes or localized header strings.
 * Corusco keeps meaning in typed column descriptors, then lets Swing display,
 * reorder, resize, sort, validate, and persist the concrete component state.</p>
 *
 * <p>Use table bindings and controllers for observable table models,
 * selection synchronization, validation decoration, header tooltips, column
 * visibility menus, and saved table layout.</p>
 *
 * <h2>Optimized Table Renderers</h2>
 *
 * <p>{@link cz.auderis.corusco.swing.table.render} provides optional renderer
 * installers for measured table hot paths. It is separate from the main table
 * package because rendering optimization is a Swing presentation concern, not
 * table metadata, row data, selection state, or persisted layout state.</p>
 *
 * <p>Start with ordinary Swing renderers and the descriptor-backed table model.
 * Use optimized renderers when profiling or workload knowledge shows that a
 * table repeatedly repaints dense timestamp, boolean, or enum-state columns.
 * The renderer package can install timestamp renderers for epoch {@code long}
 * values and finite-state renderers for booleans or enums, either by value
 * type or by a typed table {@code ColumnKey}. Returned bindings restore the
 * previous renderer during cleanup.</p>
 *
 * <p>These renderers remain EDT-confined Swing objects. Their caches are owned
 * by individual renderer instances and are deliberately bounded and
 * conservative about visual invalidation. They are not a localization system,
 * not generated metadata, and not a replacement for custom application
 * renderers when a column needs icons, rich decoration, or domain-specific
 * formatting.</p>
 *
 * <h2>Dialogs</h2>
 *
 * <p>{@link cz.auderis.corusco.swing.dialog} coordinates modal form behavior.
 * It covers active-editor commit, validation focus, dirty-state checks, cancel
 * confirmation, keyboard behavior, shell creation, and dialog lifecycle.
 * Application code still owns layout and domain workflow.</p>
 *
 * <p>The useful mental model is "core owns the form result, Swing owns the
 * temporary window". The dialog package keeps those responsibilities connected
 * without forcing every screen to reimplement OK, Cancel, dirty confirmation,
 * and validation focus rules.</p>
 *
 * <h2>Generated Forms</h2>
 *
 * <p>A generated form usually enters this module through a generated bindings
 * facade. The annotated source produces a core form model, a presentation
 * model for visual/session state, field descriptions, a generated view
 * interface, a behavior plan, and a bindings facade. The application
 * implements the view interface with Swing components.</p>
 *
 * <pre>{@code
 * CustomerEditFormModel model = new CustomerEditFormModel(originalCustomer);
 * CustomerEditPresentationModel presentation = new CustomerEditPresentationModel(model);
 * CustomerEditPanel view = new CustomerEditPanel();
 * BehaviorScope scope = new BehaviorScope();
 *
 * CustomerEditBindings.install(view, presentation, scope);
 * }</pre>
 *
 * <p>The generated facade is intentionally small. It installs the standard
 * generated component/model relationships. Layout, custom renderers, custom
 * validation summaries, service calls, and application-specific commands stay
 * in the application.</p>
 *
 * <h2>Generated Tables And Commands</h2>
 *
 * <p>A generated table source produces core table descriptions and Swing
 * helpers. The application supplies the row list and the actual {@code JTable};
 * generated helpers can install the table model and selection bindings using
 * stable descriptor data.</p>
 *
 * <p>A generated command source produces action descriptors and command
 * factories. Swing adapters then create actions for buttons, menu items,
 * toolbar items, popup menus, and key bindings without each UI location
 * duplicating the action contract.</p>
 *
 * <h2>Background Work</h2>
 *
 * <p>{@link cz.auderis.corusco.swing.task} connects core task contracts to
 * Swing. Use it when worker results, cancellation, progress, busy state, or
 * overlays must enter the UI. The package makes worker-to-EDT delivery an
 * explicit part of the screen design.</p>
 *
 * <h2>Testing</h2>
 *
 * <p>{@link cz.auderis.corusco.swing.testing} supports tests that need Swing
 * components: presenter/view wiring, generated behavior plans, bindings, table
 * state, and problem display. Prefer core-only tests for core models and use
 * Swing tests where Swing behavior is actually involved.</p>
 *
 * <p>A good test split is: validate form model behavior in core tests, command
 * state in core tests, table descriptor logic in core tests, and binding
 * propagation or EDT cleanup in Swing tests. This keeps tests fast and makes
 * UI-specific assumptions visible.</p>
 *
 * <h2>Adoption Advice</h2>
 *
 * <p>For a handwritten screen, create core models first, then bind them to
 * components. For a generated screen, inspect the generated form/table/command
 * classes and install the generated facade or helper into your Swing view.</p>
 *
 * <p>When migrating an existing Swing screen, identify the screen facts hidden
 * in the code: fields, actions, table columns, selection, validation,
 * resources, help topics, and cleanup points. Move those concepts into core
 * models or generated annotations, then replace ad hoc listeners with bindings
 * or behaviors one area at a time.</p>
 */
package cz.auderis.corusco.swing;
