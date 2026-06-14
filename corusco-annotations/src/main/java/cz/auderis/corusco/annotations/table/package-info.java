/**
 * Table annotations used to generate column descriptors and Swing table
 * binding helpers.
 *
 * <p>Use {@link cz.auderis.corusco.annotations.table.SwingTable} on a
 * non-generic record that represents one table row. Mark record components
 * that should become visible columns with
 * {@link cz.auderis.corusco.annotations.table.Column}. Generated code exposes
 * typed column keys, descriptor metadata, table resources, binding helpers,
 * and row updater functions for editable columns.</p>
 *
 * <p>Generated table key instances live in generated companions.
 * {@code <Row>Columns} exposes
 * {@code cz.auderis.corusco.core.table.TableKey} and
 * {@code cz.auderis.corusco.core.table.ColumnKey} constants.
 * {@code <Row>TableResources} exposes
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} constants for
 * headers and tooltips. Help topics from {@code @Help} are embedded in
 * descriptors as {@code cz.auderis.corusco.core.key.HelpTopic} values.</p>
 *
 * <p>Generated tables also expose
 * {@code cz.auderis.corusco.core.table.Column},
 * {@code cz.auderis.corusco.core.table.ColumnDescriptor}, and
 * {@code cz.auderis.corusco.core.table.TableDescriptor} objects through
 * {@code <Row>Columns} and {@code <Row>TableDescriptor}. Swing model and
 * selection installation helpers are generated in {@code <Row>TableBindings}.</p>
 *
 * <p>Column ids and persistence ids are stable application metadata. They can
 * appear in resources, tests, generated source, and saved table layout state,
 * so changing them should be treated as a compatibility change.</p>
 */
package cz.auderis.corusco.annotations.table;
