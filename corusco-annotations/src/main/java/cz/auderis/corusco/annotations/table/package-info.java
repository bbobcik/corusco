/**
 * Table annotations used to generate column descriptors and Swing table
 * binding helpers.
 *
 * <p>This package solves the compile-time description of typed table rows. A
 * table row record says what values exist, and {@link
 * cz.auderis.corusco.annotations.table.Column} says which of those values
 * become visible columns. The processor then emits typed column keys,
 * descriptor metadata, table resources, and row updater functions without
 * JavaBeans reflection. Packages annotated with
 * {@link cz.auderis.corusco.annotations.SwingCompanionPackage} also receive Swing table
 * binding helpers.</p>
 *
 * <p>Use {@link cz.auderis.corusco.annotations.table.CoruscoTable} on a
 * non-generic record that represents one table row. Mark record components
 * that should become visible columns with
 * {@link cz.auderis.corusco.annotations.table.Column}. Unannotated components
 * can still participate in generated editable-row constructors, but they do
 * not become columns.</p>
 *
 * <p>For example, this source record:</p>
 *
 * <pre>{@code
 * @CoruscoTable(id = "customer-table")
 * record CustomerRow(
 *         @Column(width = 180, editable = true) String name,
 *         @Column(width = 80) int orders
 * ) {
 * }
 * }</pre>
 *
 * <p>produces generated companions whose names start with
 * {@code CustomerRow}, such as {@code CustomerRowColumns},
 * {@code CustomerRowTableResources} and
 * {@code CustomerRowTableDescriptor}. Packages annotated with
 * {@link cz.auderis.corusco.annotations.SwingCompanionPackage} also receive
 * {@code CustomerRowTableBindings}. The prefix comes from the row type name,
 * so generated names remain easy to find from the source row record.</p>
 *
 * <p>The first Swing runtime step is usually to create an observable row list
 * and table model from the generated descriptor:</p>
 *
 * <pre>{@code
 * ObservableList<CustomerRow> rows = ObservableArrayList.empty();
 * ObservableTableModel<CustomerRow> model =
 *         ObservableTableModel.of(rows, CustomerRowTableDescriptor.DESCRIPTOR);
 * }</pre>
 *
 * <p>Generated table key instances live in generated companions. A columns
 * companion such as {@code CustomerRowColumns} exposes
 * {@code cz.auderis.corusco.core.table.TableKey} and
 * {@code cz.auderis.corusco.core.table.ColumnKey} constants. A resources
 * companion such as {@code CustomerRowTableResources} exposes
 * {@code cz.auderis.corusco.core.key.ResourceKey<String>} constants for
 * headers and tooltips. Help topics from {@code @Help} are embedded in
 * descriptors as {@code cz.auderis.corusco.core.key.HelpTopic} values.</p>
 *
 * <p>Generated tables also expose
 * {@code cz.auderis.corusco.core.table.Column},
 * {@code cz.auderis.corusco.core.table.ColumnDescriptor}, and
 * {@code cz.auderis.corusco.core.table.TableDescriptor} objects through
 * companions such as {@code CustomerRowColumns} and
 * {@code CustomerRowTableDescriptor}. Swing model and selection installation
 * helpers are generated for packages annotated with
 * {@link cz.auderis.corusco.annotations.SwingCompanionPackage} in companions such as
 * {@code CustomerRowTableBindings}.</p>
 *
 * <p>Editable generated columns update immutable records by calling the record
 * constructor with the edited value and the previous values for the remaining
 * components. This keeps table editing explicit and reviewable. It also means
 * row records should keep constructor invariants cheap and deterministic.</p>
 *
 * <p>Column ids and persistence ids are stable application metadata. They can
 * appear in resources, tests, generated source, and saved table layout state.
 * Use {@link cz.auderis.corusco.annotations.table.Column#persistenceId()} when
 * a visible column must keep saved layout compatibility while its generated
 * column id changes.</p>
 *
 * <p>The annotation model describes table structure, not a complete table UI.
 * Sorting, filtering, selection, state restoration, renderers, and editors are
 * installed by Swing table adapters and application code. Generated bindings
 * provide the common model and selection setup, while advanced screens can use
 * generated descriptors directly for custom assembly.</p>
 *
 * <p>Advanced users should read generated table source when reviewing a table
 * contract. Descriptor constants show ids, resource keys, persistence metadata,
 * default widths, capabilities, and accessors in one place. That generated
 * source is often the clearest compatibility review for table-state changes.</p>
 */
package cz.auderis.corusco.annotations.table;
