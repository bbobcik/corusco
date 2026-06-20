/**
 * Typed table descriptors, column definitions, and persisted table state.
 *
 * <p>This package solves the logical table problem before Swing is involved:
 * what row type is shown, which columns exist, how each column gets a value,
 * whether an edit can produce a replacement row, what text resources describe
 * a column, and which stable ids should be used for saved layout state. It is
 * deliberately free of {@code JTable}, renderers, selection models, and Event
 * Dispatch Thread rules.</p>
 *
 * <p>Use this package when table structure must be reusable, generated,
 * persisted, validated, or tested independently of a concrete UI. It is
 * especially useful for immutable record rows because a {@link
 * cz.auderis.corusco.core.table.Column} carries an explicit getter and optional
 * updater instead of relying on JavaBeans reflection or string property paths.
 * Generated {@code @CoruscoTable} records produce these descriptors for you;
 * handwritten tables can build the same objects directly.</p>
 *
 * <p>The first step is a {@link cz.auderis.corusco.core.table.TableKey} for the
 * row type and one {@link cz.auderis.corusco.core.table.ColumnKey} per column.
 * Then create {@link cz.auderis.corusco.core.table.ColumnDescriptor} values
 * with resource keys, persistence metadata, defaults, and capabilities. Finally
 * combine descriptors with row getter/updater functions into {@link
 * cz.auderis.corusco.core.table.Column} objects and place them in a {@link
 * cz.auderis.corusco.core.table.TableDescriptor}.</p>
 *
 * <pre>{@code
 * Column<CustomerRow, String> name = Column.readOnly(NAME_DESCRIPTOR, CustomerRow::name);
 * TableDescriptor<CustomerRow> descriptor = new TableDescriptor<>(TABLE, List.of(name));
 * }</pre>
 *
 * <p>Generated table companions follow the same structure. A columns companion
 * such as {@code CustomerRowColumns} exposes the table key, column keys, column
 * descriptors, and executable columns. A resource companion such as
 * {@code CustomerRowTableResources} exposes header and tooltip resource keys. A
 * descriptor companion such as {@code CustomerRowTableDescriptor} exposes the
 * ordered descriptor and helper factories. Applications should treat generated
 * ids, persistence ids, and column order as part of the preview API for the
 * screen.</p>
 *
 * <p>Descriptor ids should be treated as application-facing identifiers, not
 * incidental implementation names. A localized header may change with language,
 * and a Java accessor may change during a refactor, but a persistence id is
 * what connects saved user preferences to a column over time. When a table is
 * part of a public screen contract, review key names, resource ids, and
 * persistence ids with the same care as command ids and form field ids.</p>
 *
 * <p>Editable columns should express replacement semantics clearly. For
 * immutable record rows, a column updater returns a new row instance with one
 * value changed. For mutable row objects, the updater still needs to cooperate
 * with the observable list so downstream observers see a meaningful
 * replacement or update event. Avoid hidden reflection-based mutation because
 * it makes generated descriptors harder to test and weakens the compile-time
 * link between a column and the row constructor.</p>
 *
 * <p>Column capabilities and defaults are separate on purpose. Capabilities
 * describe what the column may do, such as editability, visibility, sorting, or
 * resizing. Defaults describe the initial presentation before user state is
 * restored. Keeping them separate lets a screen say "this column is hideable,
 * initially visible, and restored from user preferences" without overloading a
 * single boolean with several meanings.</p>
 *
 * <p>Table layout state is modeled separately from row data. A {@link
 * cz.auderis.corusco.core.table.TableStateStore} persists immutable column
 * visibility, width, order, and sort snapshots; it does not apply state to a
 * Swing {@code JTable}. Swing controllers load, merge, and write those
 * snapshots using stable persistence ids from descriptors. Migration hooks
 * transform old saved ids before merge, keeping legacy persistence concerns out
 * of current table descriptors.</p>
 *
 * <p>Validation feedback should use logical row and column identities whenever
 * possible. A sorted, filtered, or grouped table may show the same model row at
 * different view indexes over time. Routing problems through descriptors and
 * row identities keeps feedback stable while the Swing adapter decides which
 * current cell, tooltip, or decoration should display the problem.</p>
 *
 * <p>{@link cz.auderis.corusco.core.table.TableCellProblems} routes validation
 * feedback to row/column identities without tying it to a current Swing view
 * index. This is the right level for validation results produced by presenters,
 * import workflows, or background checks. Use {@code cz.auderis.corusco.swing.table}
 * to adapt descriptors to {@code JTable}, install tooltip/validation bindings,
 * track selection, and save component layout changes on the EDT.</p>
 *
 * <p>Tests at this layer should be Swing-free. Assert descriptor order, key
 * stability, resource-key wiring, getter results, updater replacement results,
 * default column state, and migration behavior for persisted state. Swing tests
 * should then verify that {@code cz.auderis.corusco.swing.table} applies the
 * same descriptor contract to a {@code JTable}.</p>
 */
package cz.auderis.corusco.core.table;
