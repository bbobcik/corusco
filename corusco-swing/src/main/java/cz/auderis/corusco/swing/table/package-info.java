/**
 * Swing table models, state controllers, and table decoration bindings for
 * Corusco typed descriptors.
 *
 * <p>{@link cz.auderis.corusco.swing.table.ObservableTableModel} adapts an
 * observable row list and a core
 * {@link cz.auderis.corusco.core.table.TableDescriptor} to Swing's
 * {@link javax.swing.table.TableModel}. Editing delegates to typed column
 * updaters so immutable row records can be replaced in the source list without
 * reflection.</p>
 *
 * <p>{@link cz.auderis.corusco.swing.table.TableStateController} owns the
 * Swing-specific side of layout persistence: it restores column order, widths,
 * visibility, and sort keys from a core table-state store, then listens to
 * header and sorter events and writes debounced snapshots. Persisted state uses
 * descriptor persistence ids, not localized header text or current view
 * indexes.</p>
 *
 * <p>Types in this package are EDT-confined. Mutate the source list on the EDT
 * while a Swing table model is subscribed, or wrap it with
 * {@link cz.auderis.corusco.swing.collection.EdtObservableList} before binding
 * it to Swing.</p>
 */
package cz.auderis.corusco.swing.table;
