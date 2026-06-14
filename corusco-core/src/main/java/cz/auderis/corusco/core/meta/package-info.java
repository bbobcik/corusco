/**
 * Generated field metadata descriptors used by forms and view builders.
 *
 * <p>This package describes presentation metadata, not current user-entered
 * values. Start with {@link cz.auderis.corusco.core.meta.FieldDescriptor},
 * which ties a typed field key to label resources, editor kind, optional help
 * metadata, and generated validation descriptors. {@link
 * cz.auderis.corusco.core.meta.FieldKind} identifies the intended editor
 * family, while {@link cz.auderis.corusco.core.meta.ConstraintDescriptor} and
 * {@link cz.auderis.corusco.core.meta.ConstraintKind} describe validation
 * constraints discovered by the annotation processor.</p>
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
