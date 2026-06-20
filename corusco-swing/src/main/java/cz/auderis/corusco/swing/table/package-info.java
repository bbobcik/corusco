/**
 * Swing table models, state controllers, and table decoration bindings for
 * Corusco typed descriptors.
 *
 * <p>This package solves the Swing side of descriptor-backed tables. The core
 * table package describes row types, columns, value extractors, edit updaters,
 * persistence ids, and table-state snapshots without depending on Swing. This
 * package turns those descriptors into live {@link javax.swing.JTable}
 * behavior: a {@link javax.swing.table.TableModel}, selection synchronization,
 * validation and tooltip decoration, header visibility controls, and persisted
 * layout restoration.</p>
 *
 * <p>Use this package when a table should be assembled from a generated or
 * handwritten {@link cz.auderis.corusco.core.table.TableDescriptor}. It is a
 * good fit for immutable record rows, generated {@code @SwingTable} metadata,
 * observable row collections, persisted column layout, and tables whose
 * selection is part of presenter state. Use plain Swing table models only when
 * the table is completely ad hoc and does not need Corusco keys, descriptors,
 * validation, or state persistence.</p>
 *
 * <p>The package assumes the descriptor is the source of truth for table
 * identity. Column model indexes, view indexes, localized headers, and current
 * sorter state are transient Swing details. Persisted state and validation
 * routing should go through descriptor keys and persistence ids so they survive
 * localization, column movement, and model refreshes.</p>
 *
 * <p>The first step is to create or obtain a descriptor and an observable row
 * collection, then adapt them through {@link
 * cz.auderis.corusco.swing.table.ObservableTableModel}:</p>
 *
 * <pre>{@code
 * ObservableList<CustomerRow> rows = ObservableArrayList.empty();
 * ObservableTableModel<CustomerRow> model =
 *         ObservableTableModel.of(rows, CustomerRowTableDescriptor.DESCRIPTOR);
 * table.setModel(model);
 * }</pre>
 *
 * <p>When the source is already an ordered read-only collection, such as a
 * sorted set or mapped projection, create a read-only model instead:</p>
 *
 * <pre>{@code
 * ObservableReadableCollection<CustomerRow> rows = sortedCustomers;
 * ObservableTableModel<CustomerRow> model =
 *         ObservableTableModel.readOnly(rows, CustomerRowTableDescriptor.DESCRIPTOR);
 * table.setModel(model);
 * }</pre>
 *
 * <p>Generated {@code @SwingTable} sources normally provide a row-specific
 * bindings helper, such as {@code CustomerRowTableBindings}, that performs
 * this setup and registers the model with a binding scope. Prefer that helper
 * for generated tables. Use the direct {@code ObservableTableModel} factory
 * when you need custom table assembly or handwritten descriptors.</p>
 *
 * <p>Generated table helpers are intentionally small. They install the model
 * and common selection bindings, but they do not choose renderers, cell
 * editors, filter widgets, popup menus, or application-specific actions. Add
 * those concerns in view code after the generated model is installed, using the
 * generated descriptors as stable metadata.</p>
 *
 * <p>{@link cz.auderis.corusco.swing.table.TableStateController} owns the
 * Swing-specific side of layout persistence. It restores column order, widths,
 * visibility, and sort keys from a core table-state store, then listens to
 * header and sorter events and writes debounced snapshots. Persisted state uses
 * descriptor persistence ids, not localized header text or current view
 * indexes. Initialize state after the table model and column model exist, and
 * close the controller with the view lifecycle.</p>
 *
 * <p>State restoration should happen after columns exist and before the user
 * starts interacting with the table. If the application migrates saved ids,
 * perform that migration at the store/controller boundary. Do not change
 * descriptor ids casually to work around old saved state; doing so can break
 * tests, resources, and existing user preferences.</p>
 *
 * <p>For selection, bind both the selected model row index and the selected row
 * value when presenter code needs stable state independent of current view
 * sorting or filtering. For validation and tooltips, use the table bindings in
 * this package so feedback is routed through typed row/column descriptors
 * rather than through current view coordinates alone.</p>
 *
 * <p>For editable tables, remember that editing calls the column updater and
 * replaces the row in the observable source list. This is suitable for
 * immutable records and generated row constructors. If rows are mutable
 * objects, make sure the updater and source list still publish a meaningful
 * change so Swing repaint and downstream observers stay coherent. Read-only
 * models observe source changes but do not call column updaters.</p>
 *
 * <p>Types in this package are EDT-confined. Mutate the source on the EDT while
 * a Swing table model is subscribed, or wrap readable sources with
 * {@link cz.auderis.corusco.swing.collection.EdtObservableReadableCollection}
 * and mutable lists with
 * {@link cz.auderis.corusco.swing.collection.EdtObservableList} before binding
 * them to Swing. Close table models, state controllers, and selection bindings
 * when the owning view is disposed.</p>
 *
 * <p>Testing table screens is usually clearer when split by layer: core tests
 * assert descriptor keys, updaters, defaults, and persistence ids; Swing tests
 * assert model row/column counts, edit propagation, selection binding, and
 * state restoration on the EDT. Generated table tests should compile and use
 * the generated companions rather than duplicating descriptor literals.</p>
 */
package cz.auderis.corusco.swing.table;
