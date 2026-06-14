/**
 * Typed identity keys for fields, resources, actions, help topics, and Swing
 * components.
 *
 * <p>This package solves Corusco's stable identity problem. Presentation code
 * needs names for fields, resources, actions, help topics, and component
 * targets, but untyped strings become hard to audit and easy to mix up. Keys
 * keep the string id while adding Java type information about the owner, value,
 * action, resource, or component being identified.</p>
 *
 * <p>Start with the key type that matches the thing being identified:
 * {@link cz.auderis.corusco.core.key.FieldKey} and
 * {@link cz.auderis.corusco.core.key.TextFieldKey} for form fields,
 * {@link cz.auderis.corusco.core.key.ResourceKey} for typed resource lookup,
 * {@link cz.auderis.corusco.core.key.ActionKey} for commands,
 * {@link cz.auderis.corusco.core.key.HelpTopic} for help dispatch, and
 * {@link cz.auderis.corusco.core.key.ComponentKey} for explicit component
 * mappings. {@link cz.auderis.corusco.core.key.KeyIds} contains shared id
 * validation helpers.</p>
 *
 * <p>Generated code uses these keys to connect annotations, metadata,
 * resources, commands, validation problems, and Swing views. The string id is a
 * boundary value that may appear in diagnostics, resource maps, generated
 * source, or persisted UI state. The generic type parameters keep the Java API
 * honest about owner types, value types, and component types.</p>
 *
 * <p>Most application code should get key instances from generated constants,
 * not by recreating ids at each use site. Generated {@code @SwingForm} records
 * with {@code @TextField} or {@code @DateField} components generate
 * {@code TextFieldKey} constants in fields companions such as
 * {@code CustomerEditFields}. Generated checkbox and combo-box components
 * generate {@code FieldKey} constants in the same companion.</p>
 *
 * <p>Generated resource keys appear in generated resource or action
 * companions. Form labels and tooltips live in companions such as
 * {@code CustomerEditResources}. Table headers and tooltips live in companions
 * such as {@code CustomerRowTableResources}. Command text and tooltip keys live
 * in action companions such as {@code CustomerPresenterActions}.</p>
 *
 * <p>Generated {@code @UiAction} methods generate {@code ActionKey} constants
 * in action companions. Generated {@code @Help(topic = ...)} metadata creates
 * {@code HelpTopic} values inside generated descriptors. Component keys are
 * currently application-created because generated view contracts expose
 * component accessor methods rather than a component registry.</p>
 *
 * <p>The first handwritten use is usually a small constant near the metadata
 * it supports:</p>
 *
 * <pre>{@code
 * static final ActionKey REFRESH = ActionKey.of("customer/refresh");
 * static final ResourceKey<String> REFRESH_TEXT =
 *         ResourceKey.of("customer/refresh/text", String.class);
 * }</pre>
 *
 * <p>Do not treat key ids as localized labels, JavaBeans property paths, or
 * arbitrary CSS-like selectors. If a user-visible string is needed, resolve a
 * {@code ResourceKey} through {@code cz.auderis.corusco.core.resource}. If a
 * persisted table column id is needed, use the table descriptor APIs in
 * {@code cz.auderis.corusco.core.table} rather than inventing ad hoc strings.</p>
 *
 * <p>The annotation package intentionally does not depend on this runtime
 * module, so annotation Javadocs name these key types by fully qualified class
 * name rather than linking across a compile-time dependency. The connection is
 * still part of the generated API contract: annotation ids become generated key
 * ids, and generated key constants are the stable handles to use at runtime.</p>
 *
 * <p>Every key type has a static {@code of(...)} factory. Use those factories
 * for handwritten metadata, tests, or integrations that do not use the
 * annotation processor. In generated-form and generated-action code, prefer the
 * generated constants so ids, owner types, and value types stay in one reviewed
 * source of truth.</p>
 *
 * <p>Advanced users should consider id migration explicitly. Keys are immutable
 * values and do not provide aliases. If a persisted id must change, table-state
 * migration, resource-map compatibility, or application-level translation
 * should handle that transition at the boundary where old ids are read.</p>
 */
package cz.auderis.corusco.core.key;
