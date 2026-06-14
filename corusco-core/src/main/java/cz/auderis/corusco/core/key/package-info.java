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
 * <p>Do not treat key ids as localized labels or reflection paths. If a user
 * visible string is needed, resolve a {@code ResourceKey} through
 * {@code cz.auderis.corusco.core.resource}. If a persisted table column id is
 * needed, use the table descriptor APIs in {@code cz.auderis.corusco.core.table}
 * rather than inventing ad hoc strings.</p>
 */
package cz.auderis.corusco.core.key;
