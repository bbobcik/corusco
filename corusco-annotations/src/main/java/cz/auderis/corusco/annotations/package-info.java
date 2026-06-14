/**
 * Source annotations that describe Corusco forms, tables, actions, validation,
 * and help metadata.
 *
 * <p>This package is the source-level vocabulary for generated Corusco UI
 * metadata. Use {@link cz.auderis.corusco.annotations.SwingForm} on a record
 * that represents editable form input and combine it with record-component
 * annotations such as {@link cz.auderis.corusco.annotations.TextField},
 * {@link cz.auderis.corusco.annotations.ComboBox}, {@link
 * cz.auderis.corusco.annotations.CheckBox}, and {@link
 * cz.auderis.corusco.annotations.DateField}. Use {@link
 * cz.auderis.corusco.annotations.SwingTable} and {@link
 * cz.auderis.corusco.annotations.Column} for table row metadata. Use {@link
 * cz.auderis.corusco.annotations.UiAction} on methods that should become
 * generated action descriptors.</p>
 *
 * <p>Constraint annotations such as {@link
 * cz.auderis.corusco.annotations.Required}, {@link
 * cz.auderis.corusco.annotations.Length}, {@link
 * cz.auderis.corusco.annotations.IntRange}, {@link
 * cz.auderis.corusco.annotations.DecimalRange}, and {@link
 * cz.auderis.corusco.annotations.Regex} contribute generated validation
 * metadata. {@link cz.auderis.corusco.annotations.Help} contributes stable
 * tooltip and help-topic ids. The processor validates annotation placement,
 * field type compatibility, ids, and selected invalid combinations.</p>
 *
 * <p>The annotations are source-retained and consumed by compile-time
 * processors through {@code javax.lang.model}. Runtime framework code should
 * use generated keys, descriptors, form models, table columns, and command
 * metadata rather than scanning annotated source types reflectively. Users edit
 * annotated records and methods; generated Java source is an output artifact.</p>
 *
 * <p>Stable ids declared here become resource keys, field keys, action keys,
 * table ids, column ids, and sometimes persisted UI-state ids. Treat id changes
 * as compatibility changes for generated code and saved table preferences.</p>
 */
package cz.auderis.corusco.annotations;
