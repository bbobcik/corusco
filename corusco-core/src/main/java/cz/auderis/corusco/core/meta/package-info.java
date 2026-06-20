/**
 * Generated field metadata descriptors used by forms and view builders.
 *
 * <p>This package describes presentation metadata, not current user-entered
 * values. Start with {@link cz.auderis.corusco.core.meta.FieldDescriptor},
 * which ties a typed field key to label resources, editor kind, optional help
 * metadata, and generated validation descriptors. {@link
 * cz.auderis.corusco.core.meta.FieldKind} identifies the older editor family.
 * Newer descriptors use {@link
 * cz.auderis.corusco.core.meta.EditorDescriptor} so richer editor families such
 * as radio groups can be described without flattening every control into the
 * older field-kind enum. {@link
 * cz.auderis.corusco.core.meta.ConstraintDescriptor} and {@link
 * cz.auderis.corusco.core.meta.ConstraintKind} describe validation constraints
 * discovered by the annotation processor.</p>
 *
 * <p>{@link cz.auderis.corusco.core.meta.OptionDescriptor} describes one
 * selectable option value together with generated resource keys for label,
 * description, and help text. {@link
 * cz.auderis.corusco.core.meta.FieldDependency} describes a generated
 * dependency from a source field to a component-state model, with expected
 * values normalized by generated code rather than compared as free-form
 * strings.</p>
 *
 * <p>Annotation types in {@code cz.auderis.corusco.annotations} are the source
 * declarations. Processor output turns them into these runtime descriptors so
 * forms, resources, accessibility helpers, tooltips, and generated view plans
 * can share the same stable ids. The actual editable state lives in
 * {@code cz.auderis.corusco.core.form}; the UI component plumbing lives in
 * {@code cz.auderis.corusco.swing.binding} and
 * {@code cz.auderis.corusco.swing.behavior}.</p>
 *
 * <p>Use descriptors when building generic form infrastructure or when tests
 * need to assert generated metadata. Do not treat descriptor ids as localized
 * text, and do not use this package as a storage model for domain values.</p>
 */
package cz.auderis.corusco.core.meta;
