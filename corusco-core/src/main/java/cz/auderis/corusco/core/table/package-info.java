/**
 * Typed table keys, descriptors, column definitions, and table state stores.
 *
 * <p>APIs in this package are Swing-free and avoid JavaBeans reflection or
 * public string property paths. Generated code should emit stable table and
 * column key constants plus explicit extractor/updater functions. Table state
 * stores persist immutable state snapshots and do not apply anything directly
 * to Swing components. Schema migration hooks transform loaded state before
 * descriptor merge, keeping old persistence ids out of live table code.</p>
 */
package cz.auderis.corusco.core.table;
