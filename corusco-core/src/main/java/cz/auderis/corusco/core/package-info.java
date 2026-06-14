/**
 * Toolkit-neutral presentation model runtime used by Corusco applications and
 * generated code.
 *
 * <h2>What Belongs In Core</h2>
 *
 * <p>Core is the part of Corusco that can be used without opening a Swing
 * window. It contains the models and contracts behind a screen: field values,
 * dirty state, validation problems, commands, resource keys, help topics,
 * observable lists, table descriptions, lifecycle handles, and task
 * contracts.</p>
 *
 * <p>Core does not create buttons, fields, dialogs, or tables. It gives those
 * UI concepts stable Java models so they can be tested and reused before Swing
 * components are involved. The Swing module later adapts these models to
 * {@code JTextField}, {@code JButton}, {@code JTable}, dialogs, menus, and
 * other concrete components.</p>
 *
 * <p>A model is the current state that the application owns. Examples include
 * the text typed into a form field, the selected row in a table, whether Save
 * is enabled, and the list of validation problems. Models change over time and
 * can be observed by bindings or presenters.</p>
 *
 * <p>A descriptor is an immutable description of a UI concept. It answers
 * questions such as: what is this field's stable id, what resource key contains
 * its label, what columns does this table have, what shortcut belongs to this
 * command, or what type of value does this column contain? Descriptors do not
 * mutate state and do not own Swing components.</p>
 *
 * <p>A presenter is application code that owns screen behavior. Core does not
 * force a presenter base class, but presenters commonly use core values,
 * commands, validators, form models, row lists, and lifecycle scopes. The
 * presenter decides when data is loaded, when a command is enabled, and what
 * happens after a user action.</p>
 *
 * <h2>Why Not Store Everything In Swing Components?</h2>
 *
 * <p>Plain Swing makes it easy to put important state in many places: document
 * listeners, action objects, table-model indexes, component client properties,
 * localized header strings, focus listeners, and dialog button handlers. That
 * is convenient at first, but it makes behavior hard to test, hard to
 * refactor, and hard to share between generated and handwritten code.</p>
 *
 * <p>Core moves the important screen contract into explicit Java objects. A
 * field model can be tested without a {@code JTextField}. A command can be
 * enabled or executed without a {@code JButton}. A table descriptor can be
 * reviewed without a {@code JTable}. Swing then becomes an adapter layer
 * rather than the only place where state exists.</p>
 *
 * <h2>First Navigation Map</h2>
 *
 * <ul>
 *   <li>Use {@link cz.auderis.corusco.core.form} for editable forms.</li>
 *   <li>Use {@link cz.auderis.corusco.core.table} for table columns and saved
 *       table layout state.</li>
 *   <li>Use {@link cz.auderis.corusco.core.command} for Save, Delete, Refresh,
 *       Toggle, and other user actions.</li>
 *   <li>Use {@link cz.auderis.corusco.core.value} for one observable value,
 *       such as selected row or busy state.</li>
 *   <li>Use {@link cz.auderis.corusco.core.collection} for observable lists
 *       used by tables, combo boxes, and master/detail screens.</li>
 *   <li>Use {@link cz.auderis.corusco.core.validation} and
 *       {@link cz.auderis.corusco.core.problem} for validation rules and
 *       structured validation results.</li>
 * </ul>
 *
 * <h2>Typed Identity</h2>
 *
 * <p>{@link cz.auderis.corusco.core.key} contains typed keys. A field key,
 * action key, resource key, component key, table key, column key, and help
 * topic are different Java concepts even if each wraps a string id. This keeps
 * APIs clear: a resource key cannot accidentally be passed where an action key
 * is expected.</p>
 *
 * <p>Many keys are produced by generated annotation companions. Handwritten
 * code can create keys directly when it defines its own fields, commands,
 * resources, or tables. Either way, key ids should be stable when resources,
 * tests, help mapping, or saved table preferences depend on them.</p>
 *
 * <h2>Observable State</h2>
 *
 * <p>{@link cz.auderis.corusco.core.value} is for one value that other code can
 * observe. Use it for selected rows, enabled flags, busy flags, status text,
 * derived labels, and master/detail state. Values are synchronous and
 * Swing-free.</p>
 *
 * <pre>{@code
 * WritableValue<CustomerRow> selectedCustomer = SimpleValue.empty();
 * Subscription subscription = selectedCustomer.subscribe(event ->
 *         details.show(event.newValue()));
 *
 * selectedCustomer.setValue(row, ChangeOrigin.USER);
 * subscription.close();
 * }</pre>
 *
 * <p>{@link cz.auderis.corusco.core.collection} is the list counterpart. Use
 * it for row lists, combo-box options, filtered views, sorted views, mapped
 * views, and loadable collections. Swing list and table models can adapt these
 * lists later.</p>
 *
 * <pre>{@code
 * ObservableArrayList<CustomerRow> rows = ObservableArrayList.empty();
 * rows.subscribe(changeSet -> auditRows(changeSet));
 *
 * rows.batch(list -> {
 *     list.add(new CustomerRow("Ada", true));
 *     list.add(new CustomerRow("Grace", false));
 * });
 * }</pre>
 *
 * <h2>Forms</h2>
 *
 * <p>{@link cz.auderis.corusco.core.form} is for transactional editing. A form
 * model tracks raw text, parsed values, touched state, dirty state, validation
 * problems, reset behavior, baseline acceptance, and result creation. This is
 * what lets a user type invalid intermediate text without corrupting the last
 * valid semantic value.</p>
 *
 * <pre>{@code
 * final class CustomerForm extends AbstractFormModel<Customer> {
 *     final TextFieldModel<Customer, String> name =
 *             register(new TextFieldModel<>(CustomerFields.NAME, "", Converters.string()));
 *
 *     @Override
 *     protected Customer createResult() {
 *         return new Customer(name.value());
 *     }
 * }
 * }</pre>
 *
 * <p>{@link cz.auderis.corusco.core.convert} handles text conversion. Parsing
 * answers "can this text become a value?". {@link
 * cz.auderis.corusco.core.validation} handles semantic validation. Validation
 * answers "is this value acceptable for this form?". Keeping those questions
 * separate makes error reporting clearer.</p>
 *
 * <h2>Problems And Validation</h2>
 *
 * <p>{@link cz.auderis.corusco.core.problem} represents validation and parsing
 * issues as structured data: severity, stable problem code, source, target,
 * and message. A problem may later become an inline message, tooltip, dialog
 * summary, disabled OK button, table-cell decoration, or test assertion.</p>
 *
 * <p>{@link cz.auderis.corusco.core.validation} provides synchronous and
 * asynchronous validation contracts. Validators return problem sets rather
 * than throwing for normal user mistakes. This allows generated validators,
 * handwritten validators, forms, dialogs, and table feedback to cooperate.</p>
 *
 * <h2>Commands</h2>
 *
 * <p>{@link cz.auderis.corusco.core.command} represents user actions. A
 * command is more than a callback: it has stable identity, display
 * description, enabled state, optional selected state, optional shortcut data,
 * and an executable handler. Swing buttons and menu items are adapters around
 * commands, not the source of command identity.</p>
 *
 * <pre>{@code
 * ActionDescriptor descriptor =
 *         ActionDescriptor.action(CustomerActions.SAVE, CustomerResources.SAVE_TEXT);
 * MutableCommand save =
 *         CommandFactory.command(descriptor, command -> form.toResult());
 *
 * save.setEnabled(form.isCommittable());
 * }</pre>
 *
 * <h2>Tables</h2>
 *
 * <p>{@link cz.auderis.corusco.core.table} describes tables before a
 * {@code JTable} exists. A table descriptor names the row type and ordered
 * columns. Each column has a stable key, label resources, default width,
 * persisted-layout id, value type, read function, and optionally an update
 * function for editable cells.</p>
 *
 * <p>This avoids a common Swing table problem: important meaning gets attached
 * to integer column indexes or localized header strings. Corusco stores table
 * meaning in typed descriptors, and Swing adapters translate that descriptor
 * into a concrete table model and column model.</p>
 *
 * <h2>Resources, Help, Tooltips, And Metadata</h2>
 *
 * <p>{@link cz.auderis.corusco.core.resource} resolves user-facing text from
 * typed resource keys. Core descriptors refer to resource keys; applications
 * decide whether those keys come from maps, bundles, layered resources, or
 * test doubles.</p>
 *
 * <p>{@link cz.auderis.corusco.core.help} represents help topics and help
 * requests. Swing may bind those topics to F1, context help, or buttons, but
 * core stays independent of the final help viewer.</p>
 *
 * <p>{@link cz.auderis.corusco.core.tooltip} models tooltip content and
 * composition policy. Tooltips often combine static help, validation problems,
 * disabled reasons, and help indicators, so treating them as structured data
 * keeps Swing bindings consistent.</p>
 *
 * <p>{@link cz.auderis.corusco.core.meta} contains generic field and
 * constraint descriptions generated from annotations. Application code can
 * inspect generated fields and validation declarations without scanning
 * annotations at runtime.</p>
 *
 * <h2>Lifecycle And Tasks</h2>
 *
 * <p>{@link cz.auderis.corusco.core.lifecycle} contains cleanup contracts:
 * subscriptions, disposable objects, detachable scopes, and subscription
 * scopes. The object that registers a listener should own the handle that
 * removes it. This prevents duplicate listeners and stale callbacks after a
 * screen closes.</p>
 *
 * <p>{@link cz.auderis.corusco.core.task} contains toolkit-neutral task
 * contracts: cancellation, callbacks, generation counters, task handles, and
 * task services. Swing task helpers add the worker-thread-to-EDT boundary
 * later.</p>
 *
 * <h2>Generated And Handwritten Code Meet Here</h2>
 *
 * <p>Generated annotation output and handwritten code are expected to meet at
 * the core layer. A generated form model is still a core form model. A
 * generated table descriptor is still a core table descriptor. A generated
 * command factory still returns core commands.</p>
 *
 * <p>This is why core should be the default place for tests. Test form logic,
 * validators, command state, table descriptors, observable values, and
 * observable lists without constructing Swing components. Then add Swing tests
 * only for Swing-specific binding and component behavior.</p>
 *
 * <h2>Threading Model</h2>
 *
 * <p>Core is synchronous and does not marshal work to the Swing Event Dispatch
 * Thread. Listener notifications generally happen during the mutating call. If
 * a core model is bound to Swing, the Swing adapter or application code must
 * apply the EDT rules documented by {@code cz.auderis.corusco.swing}.</p>
 *
 * <p>That explicitness is intentional. Core tests stay fast and predictable,
 * and thread boundaries are visible where they matter: task services, Swing
 * adapters, or application dispatch code.</p>
 */
package cz.auderis.corusco.core;
