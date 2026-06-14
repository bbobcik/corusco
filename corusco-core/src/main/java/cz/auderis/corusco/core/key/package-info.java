/**
 * Typed identity keys for fields, resources, actions, help topics, and Swing
 * components.
 *
 * <p>This package gives Corusco stable names without falling back to untyped
 * string property paths. Start with {@link cz.auderis.corusco.core.key.KeyIds}
 * for validation helpers and with the specific key type that matches the thing
 * being identified: {@link cz.auderis.corusco.core.key.FieldKey} and
 * {@link cz.auderis.corusco.core.key.TextFieldKey} for form fields,
 * {@link cz.auderis.corusco.core.key.ResourceKey} for typed resource lookup,
 * {@link cz.auderis.corusco.core.key.ActionKey} for commands,
 * {@link cz.auderis.corusco.core.key.HelpTopic} for help dispatch, and
 * {@link cz.auderis.corusco.core.key.ComponentKey} for Swing component
 * mappings.</p>
 *
 * <p>Generated code uses these keys to connect annotations, metadata,
 * resources, commands, validation problems, and Swing views. The string id is a
 * boundary value that may appear in diagnostics, resource maps, generated
 * source, or persisted UI state. The generic type parameters keep the Java API
 * honest about owner types, value types, and component types.</p>
 *
 * <p>Most application code should get key instances from generated constants,
 * not by recreating ids at each use site. The main generated sources are:</p>
 *
 * <ul>
 *   <li>{@code @SwingForm} records with {@code @TextField} or
 *       {@code @DateField} components generate
 *       {@code TextFieldKey} constants in {@code <Form>Fields}.</li>
 *   <li>{@code @SwingForm} records with {@code @CheckBox} or
 *       {@code @ComboBox} components generate {@code FieldKey} constants in
 *       {@code <Form>Fields}.</li>
 *   <li>{@code @SwingForm}, form field annotations, {@code @Help}, table
 *       {@code @Column}, and command {@code @UiAction} metadata generate typed
 *       {@code ResourceKey<String>} constants in generated resource/action
 *       companion classes.</li>
 *   <li>{@code @UiAction} methods generate {@code ActionKey} constants in
 *       {@code <Owner>Actions}.</li>
 *   <li>{@code @Help(topic = ...)} on generated form fields or table columns
 *       produces {@code HelpTopic} values inside generated descriptors.</li>
 * </ul>
 *
 * <p>The annotation package intentionally does not depend on this runtime
 * module, so annotation Javadocs name these key types by fully qualified class
 * name rather than linking across a compile-time dependency. The connection is
 * still part of the generated API contract: annotation ids become generated key
 * ids, and generated key constants are the stable handles to use at runtime.</p>
 *
 * <p>Do not treat key ids as localized labels or reflection paths. If a user
 * visible string is needed, resolve a {@code ResourceKey} through
 * {@code cz.auderis.corusco.core.resource}. If a persisted table column id is
 * needed, use the table descriptor APIs in {@code cz.auderis.corusco.core.table}
 * rather than inventing ad hoc strings.</p>
 *
 * <p>Every key type also has a static {@code of(...)} factory. Use those
 * factories for handwritten metadata, tests, or integrations that do not use
 * the annotation processor. In generated-form and generated-action code,
 * prefer the generated constants so ids, owner types, and value types stay in
 * one reviewed source of truth.</p>
 */
package cz.auderis.corusco.core.key;
