/**
 * Typed table descriptors, column definitions, and persisted table state.
 *
 * <p>This package models a table independently of any Swing component.
 * {@link cz.auderis.corusco.core.table.TableDescriptor} names the row type and
 * ordered columns; each {@link cz.auderis.corusco.core.table.Column} combines
 * immutable metadata with explicit row getter/updater functions. The API avoids
 * JavaBeans reflection and public string property paths, which lets generated
 * code represent immutable record rows safely.</p>
 *
 * <p>Start with {@link cz.auderis.corusco.core.table.TableDescriptor} when you
 * need to describe the columns available for a row type. {@link
 * cz.auderis.corusco.core.table.ColumnDescriptor}, {@link
 * cz.auderis.corusco.core.table.ColumnKey}, {@link
 * cz.auderis.corusco.core.table.ColumnCapabilities}, and {@link
 * cz.auderis.corusco.core.table.ColumnDefaults} describe presentation and edit
 * capabilities for a column. {@link cz.auderis.corusco.core.table.TableCellProblems}
 * routes validation feedback to row/column identities without tying it to a
 * current Swing view index.</p>
 *
 * <p>Table layout state is modeled separately from row data. A
 * {@link cz.auderis.corusco.core.table.TableStateStore} persists immutable
 * column visibility, width, order, and sort snapshots; it does not apply state
 * to a Swing {@code JTable}. Swing controllers load, merge, and write those
 * snapshots using stable persistence ids from descriptors. Migration hooks
 * transform old saved ids before merge, keeping legacy persistence concerns out
 * of current table descriptors.</p>
 *
 * <p>Use this package to define and persist the logical table contract. Use
 * {@code cz.auderis.corusco.swing.table} to adapt descriptors to
 * {@code JTable}, install tooltip/validation bindings, track selection, and
 * save component layout changes on the EDT.</p>
 */
package cz.auderis.corusco.core.table;
