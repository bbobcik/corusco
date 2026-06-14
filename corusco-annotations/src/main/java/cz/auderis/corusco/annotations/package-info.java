/**
 * Compile-time annotations for generating Corusco UI support code.
 *
 * <h2>What This Module Is For</h2>
 *
 * <p>Use these annotations when a Swing screen has stable facts that should be
 * declared once and reused consistently. Typical facts are form field ids,
 * table column ids, labels, tooltips, validation limits, help topics, command
 * ids, and keyboard shortcuts. Without generation, those facts often end up
 * copied between component constructors, table models, validators, resource
 * bundles, tests, and action listeners.</p>
 *
 * <p>An annotation is only the input. It does not create a Swing component at
 * runtime. The annotation processor reads the annotated source during
 * compilation and writes ordinary Java classes that the application can import
 * and use. Those generated classes are what connect annotated source to the
 * core and Swing runtime modules.</p>
 *
 * <p>A generated companion is one of those generated classes. For example, a
 * source record named {@code CustomerEdit} may produce classes such as
 * {@code CustomerEditFields}, {@code CustomerEditFormModel},
 * {@code CustomerEditView}, {@code CustomerEditBehaviorPlan}, and
 * {@code CustomerEditBindings}. They are generated next to the annotated
 * source and are consumed like normal Java API.</p>
 *
 * <p>A descriptor is an immutable object that describes part of a screen before
 * any Swing component is created. A field descriptor says what field exists and
 * what kind of editor it expects. A table descriptor says which columns exist,
 * how to read cell values, how an edit updates a row, and which column ids are
 * stable for saved layout. An action descriptor says which text, tooltip,
 * mnemonic, and shortcut belong to a command.</p>
 *
 * <p>A presenter is the object that owns screen behavior. Some applications
 * call it a controller. It reacts to commands such as Save, Delete, Refresh,
 * or Toggle Details; it calls services; it enables or disables actions; and it
 * updates models. Corusco can generate command support around annotated
 * presenter methods, but the application still owns the behavior.</p>
 *
 * <h2>Where To Start</h2>
 *
 * <p>If the user edits values, start with {@link
 * cz.auderis.corusco.annotations.form}. A record annotated with
 * {@code @SwingForm} describes the values in the form. Record components
 * annotated with {@code @TextField}, {@code @DateField}, {@code @CheckBox},
 * or {@code @ComboBox} describe the intended editor family.</p>
 *
 * <pre>{@code
 * @SwingForm(id = "customer/edit")
 * public record CustomerEdit(
 *         @TextField
 *         @Required
 *         @Length(max = 80)
 *         String name,
 *
 *         @ComboBox
 *         CustomerType type
 * ) {
 * }
 * }</pre>
 *
 * <p>This form source gives the processor enough information to generate field
 * keys, label and tooltip resource keys, field descriptors, validation
 * descriptors, a form model, a Swing view contract, a behavior plan, and a
 * bindings facade. The generated form model owns editable state; the generated
 * bindings facade installs Swing bindings into an application-provided view.</p>
 *
 * <p>If the user sees a list of rows in a {@code JTable}, start with {@link
 * cz.auderis.corusco.annotations.table}. A record annotated with
 * {@code @SwingTable} describes one row. Components annotated with
 * {@code @Column} become table columns.</p>
 *
 * <pre>{@code
 * @SwingTable(id = "customer/list")
 * public record CustomerRow(
 *         @Column(header = "customer/list/name/header", width = 180)
 *         String name,
 *
 *         @Column(editable = true, width = 90)
 *         boolean active
 * ) {
 * }
 * }</pre>
 *
 * <p>The generated table descriptor is the object that says, in Java, "this
 * table has these columns, in this order, with these ids, labels, default
 * widths, and row access functions". The {@code JTable} is not configured
 * from scattered column indexes and header strings; it is configured from one
 * typed table description.</p>
 *
 * <p>If a button, menu item, toolbar item, or keyboard shortcut should invoke
 * application behavior, start with {@link
 * cz.auderis.corusco.annotations.command}. A no-argument method annotated with
 * {@code @UiAction} becomes generated command support.</p>
 *
 * <pre>{@code
 * public final class CustomerPresenter {
 *     @UiAction(id = "customer/save", text = "customer/save/text")
 *     void save() {
 *         // Commit the current form model.
 *     }
 * }
 *
 * CommandSet commands = CustomerPresenterActions.commands(presenter);
 * }</pre>
 *
 * <p>The generated command support contains stable action keys, resource keys,
 * action descriptors, and factories that create command objects bound to the
 * presenter instance. Swing adapters can then use the same command for a
 * button, a menu item, and a keyboard shortcut.</p>
 *
 * <h2>Validation And Help</h2>
 *
 * <p>Use {@link cz.auderis.corusco.annotations.validation} for simple field
 * constraints that are natural to declare next to a field. Examples are
 * required values, string length limits, regular expressions, integer ranges,
 * decimal ranges, and related declarative checks supported by the processor.</p>
 *
 * <p>Validation annotations are intentionally limited. They are good for
 * local, deterministic rules. Rules that call services, compare many objects,
 * depend on permissions, or require business workflow should remain normal
 * Java code using {@code cz.auderis.corusco.core.validation}.</p>
 *
 * <p>Use {@link cz.auderis.corusco.annotations.help} when a field, action, or
 * component has a stable help topic. The annotation records the topic; the
 * application still decides how help is displayed, whether that means a local
 * help window, web documentation, a search panel, or no visible help in a
 * particular build.</p>
 *
 * <h2>Generated Names And Stable Ids</h2>
 *
 * <p>Ids in annotations are part of the UI contract. They become input to
 * generated field keys, table keys, column keys, action keys, resource keys,
 * problem codes, and sometimes persisted table layout ids. Changing an id can
 * affect generated source, resource lookup, tests, help mapping, and saved user
 * preferences.</p>
 *
 * <p>Choose ids as durable UI names, not as temporary implementation notes. A
 * Java accessor may be renamed during refactoring, and a visible label may be
 * translated, but a persisted column id or action id should usually remain
 * stable.</p>
 *
 * <p>The generated Java class names intentionally mirror the annotated source
 * name. A record named {@code CustomerEdit} leads to generated names that start
 * with {@code CustomerEdit}. This makes generated output easy to discover in
 * IDE completion and easy to inspect during reviews.</p>
 *
 * <h2>Why Generation Instead Of Runtime Reflection</h2>
 *
 * <p>Traditional Swing code often relies on runtime conventions: string
 * property names, localized header text, JavaBeans reflection, anonymous
 * listeners, and manually synchronized action objects. Those approaches can be
 * workable at small scale, but they are easy to break during refactoring and
 * difficult to test without opening UI components.</p>
 *
 * <p>Corusco generation moves those contracts into compiler-visible Java. If a
 * record component disappears, generated code changes. If a command method has
 * the wrong shape, compilation can report it. If a table column is editable,
 * the generated column can call the record constructor directly instead of
 * mutating a row by reflective property name.</p>
 *
 * <p>The annotations are source-retained for that reason. Runtime framework
 * code should not scan annotations reflectively. Runtime code should consume
 * the generated classes and the public core/Swing APIs.</p>
 *
 * <h2>Typical Adoption Path</h2>
 *
 * <p>Start with one small form, table, or presenter. Annotate the source, run
 * compilation, and inspect the generated source. Seeing the generated class
 * names and constants is the fastest way to learn the Corusco mental model.</p>
 *
 * <p>Next, wire the generated output into a small Swing view. For a form, that
 * usually means constructing the generated form model, implementing the
 * generated view interface with Swing components, creating a binding scope,
 * and calling the generated bindings facade.</p>
 *
 * <p>When migrating an existing screen, look for repeated screen facts first:
 * resource keys, action names, keyboard shortcuts, table columns, validation
 * limits, field ids, and help topics. Those are strong candidates for
 * annotations. Layout, renderers, service calls, permissions, and business
 * workflows usually remain handwritten.</p>
 *
 * <p>Read the focused annotation package pages for exact attributes and
 * generated class names. This overview explains the vocabulary and the normal
 * path from annotated source to generated Java support code.</p>
 */
package cz.auderis.corusco.annotations;
