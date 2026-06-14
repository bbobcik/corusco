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
 * <p>Column ids and persistence ids are stable application metadata. They can
 * appear in resources, tests, generated source, and saved table layout state,
 * so changing them should be treated as a compatibility change.</p>
 */
package cz.auderis.corusco.annotations.table;
