# Corusco Table Guide

Corusco tables use typed descriptors instead of JavaBeans property names,
reflection, or ad hoc Swing column constants. A table descriptor names the row
type, stable table id, typed columns, resource keys, help metadata, default
layout, edit capabilities, and persistence ids. Swing adapters then turn that
descriptor into an ordinary `JTable` model and controller set.

Use the table stack when a screen shows immutable row values, generated column
metadata, persisted user column layout, or row pipelines already owned by
Glazed Lists:

```text
row source
    -> ObservableList<R> or GlazedObservableList<R>
        -> ObservableTableModel<R>
            -> JTable
            -> selection binding
            -> table-state controller and visibility menu
```

## Core Types

| Type | Role |
| --- | --- |
| `TableKey<R>` | Stable typed table identity. |
| `ColumnKey<R, V>` | Stable typed column identity for a row/value pair. |
| `ColumnDescriptor<R, V>` | Immutable column metadata: resource keys, help topic, persistence metadata, defaults, and capabilities. |
| `Column<R, V>` | Executable column with direct getter and optional row-updater function. |
| `TableDescriptor<R>` | Ordered descriptor containing the table key and executable columns. |
| `ObservableTableModel<R>` | EDT-confined Swing table model backed by an `ObservableList<R>`. |
| `TableSelectionBinding<R>` | Binds JTable selection to selected model-row and selected-row values. |
| `TableState` | Swing-free persisted visual layout and sort state. |
| `TableStateController<R>` | Restores, observes, saves, and flushes JTable column/sort state. |
| `TableHeaderColumnVisibilityMenu<R>` | Header popup menu that toggles descriptor columns through the state controller. |

Column access is explicit Java code. Editable columns return a replacement row,
which fits immutable records and generated updater helpers.

## Generated Table Records

Annotate a non-generic record with `@CoruscoTable` and annotate at least one
record component with `@Column`:

```java
@CoruscoTable(id = "generated-customer-table")
record GeneratedCustomerRow(
        @Column(
                persistenceId = "generated-customer-table/customer-name",
                width = 180,
                minWidth = 80,
                maxWidth = 320,
                editable = true
        )
        @Help(tooltip = "generated-customer-table/name/help", topic = "generated-customer-table/name")
        String name,
        @Column(width = 80, sortable = false) int orders
) {
}
```

The processor currently generates these companions:

- `GeneratedCustomerRowColumns`
- `GeneratedCustomerRowTableResources`
- `GeneratedCustomerRowTableDescriptor`
- `GeneratedCustomerRowTableBindings`

`GeneratedCustomerRowColumns` exposes the table key, column keys, descriptors,
and executable columns. Primitive record components are represented by boxed
column key value types, so an `int orders` component becomes a
`ColumnKey<GeneratedCustomerRow, Integer>`.

Editable generated columns call a generated updater helper. The helper invokes
the record constructor with the edited component value and the unchanged values
from the other record accessors.

## Installing a Generated Table

Generated binding helpers install the descriptor-backed model and place model
cleanup under the same lifecycle scope as other Swing bindings:

```java
BasicEventList<GeneratedCustomerRow> eventList = new BasicEventList<>(new ArrayList<>(List.of(
        new GeneratedCustomerRow("Acme", 2),
        new GeneratedCustomerRow("Globex", 5)
)));
GlazedObservableList<GeneratedCustomerRow> rows = GlazedListsAdapters.observableList(eventList);

try (BindingScope scope = new BindingScope()) {
    JTable table = new JTable();
    ObservableTableModel<GeneratedCustomerRow> model =
            GeneratedCustomerRowTableBindings.installModel(table, rows, scope);

    TableStateController<GeneratedCustomerRow> stateController = scope.add(
            TableStateController.install(table, model, tableStateStore)
    );
    TableHeaderColumnVisibilityMenu<GeneratedCustomerRow> visibilityMenu = scope.add(
            TableHeaderColumnVisibilityMenu.install(table, model, stateController)
    );

    visibilityMenu.createMenu();
}
rows.close();
```

Create and use `ObservableTableModel` on the EDT. If the backing row source can
change off the EDT, place an explicit EDT-dispatching observable-list adapter in
front of the model. Without that boundary, off-EDT source changes fail fast
instead of firing Swing events on the wrong thread.

## Glazed Lists Row Sources

Glazed Lists is a first-class row-source option. Use `corusco-glazedlists` when
existing screens already own `EventList` pipelines:

```java
BasicEventList<GeneratedCustomerRow> eventList = new BasicEventList<>(new ArrayList<>(List.of(
        new GeneratedCustomerRow("Acme", 2),
        new GeneratedCustomerRow("Globex", 5)
)));
GlazedObservableList<GeneratedCustomerRow> observableRows =
        GlazedListsAdapters.observableList(eventList);
```

The adapter implements `ObservableList`, so generated descriptors and
`ObservableTableModel` do not need a separate table-model path for `EventList`.
The wrapped `EventList` remains the storage owner. Close the adapter when the
Corusco-side lifecycle ends; closing removes the Glazed Lists listener but does
not dispose the source list.

The adapter translates inserts, deletes, updates, moves, clears, and reorder
events into Corusco list changes. `ObservableTableModel` uses those precise
events to fire narrow Swing table updates where possible.

## Selection Binding

Selection binding converts between JTable view rows and stable model rows. This
matters when a sorter or filter changes the visible order:

```java
SimpleValue<Integer> selectedModelRow = SimpleValue.empty();
SimpleValue<GeneratedCustomerRow> selectedRow = SimpleValue.empty();

GeneratedCustomerRowTableBindings.bindSelection(
        table,
        model,
        selectedModelRow,
        selectedRow,
        scope
);
```

User selection writes the model-row value and optional row value with
`ChangeOrigin.USER`. Presenter-driven changes to the selected model-row value
select the corresponding current JTable view row. Configure the row sorter
before creating the binding; if the table receives a replacement sorter, close
and recreate the binding.

## Table State

`TableState` is Swing-free. It stores table id, column persistence ids, widths,
orders, visibility, sort ids, sort directions, and schema version. It does not
store `TableColumn` instances or view indexes.

`TableStateController` performs the Swing-specific bridge:

- loads state by generated table id;
- applies an optional `TableStateMigration`;
- merges stored state with the current descriptor;
- drops unknown old columns;
- appends new descriptor columns;
- clamps restored widths to current bounds;
- maps Swing column movement, resize, visibility, and sorter state back to
  stable persistence ids;
- debounces event-triggered saves and flushes on close.

Use explicit persistence ids for columns whose serialized identity must survive
component renames:

```java
@Column(
        persistenceId = "generated-customer-table/customer-name",
        width = 180,
        minWidth = 80,
        maxWidth = 320
)
String name
```

When a released table changes ids, provide a `TableStateMigration<R>` that maps
known old ids before descriptor merge handles unknown or newly added columns.

## Header Visibility

`TableHeaderColumnVisibilityMenu` installs a popup listener on the table header.
It rebuilds menu items from the current descriptor and captured state each time
the popup opens, then delegates visibility changes to `TableStateController`.

The controller owns hidden `TableColumn` instances, so callers should use
`setColumnVisible(columnId, visible)` or the header menu instead of removing
columns directly from Swing.

## Handwritten Descriptors

Generated descriptors are the preferred path, but handwritten tables use the
same runtime shape:

```java
Column<CustomerRow, String> name = Column.editable(
        new ColumnDescriptor<>(
                ColumnKey.of("name", CustomerRow.class, String.class),
                ResourceKey.of("customers.name", String.class),
                null,
                ColumnDefaults.visible(160, 0),
                ColumnCapabilities.editableColumn()
        ),
        CustomerRow::name,
        (row, value) -> new CustomerRow(value, row.orders())
);
```

This is intentionally ordinary Java: no property strings, no reflective
accessors, and no hidden dependency on JavaBeans naming.

## Testing Tables

Prefer focused tests that exercise the descriptor and binding behavior without
opening windows:

- table model column count, column class, names, and cell values;
- editable columns replacing immutable rows in the row source;
- precise row-change propagation from `ObservableList` and Glazed Lists;
- sorted JTable selection mapping back to model rows;
- table-state merge, migration, width clamping, sort filtering, and save flush;
- header visibility menu item count and controller delegation;
- generated source shape for keys, descriptors, resources, updaters, and
  binding helpers.

See `GeneratedTableColumnsExample`, `ObservableTableModelExample`,
`TableSelectionBindingExample`, `TableStateExample`, and
`GlazedListsInteropExample` for compiling scenarios with method-body comments.

## Current Limits

- Generated table metadata currently targets non-generic records.
- `@Column` component types must be primitive or declared types.
- Generated editable column updaters rebuild rows through the record
  constructor; mutable JavaBean rows are not supported.
- Generated table models assume an `ObservableList` row source. Use
  `GlazedListsAdapters.observableList(...)` for Glazed Lists `EventList`
  pipelines.
- `ObservableTableModel` is EDT-confined and does not dispatch source-list
  events by itself.
- Header resource lookup is represented by resource keys; user-facing
  localization lookup is still application-owned at this stage.
