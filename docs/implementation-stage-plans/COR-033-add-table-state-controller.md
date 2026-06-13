# COR-033 Add Table State Controller

Commit message:

```text
COR-033 Add table state controller
```

## Roadmap Coverage

Roadmap Stage 14: Table State Persistence, Swing controller slice.

## Objective

Add an EDT-confined Swing controller that restores table column layout and sort
state from a `TableStateStore`, keeps hidden columns addressable by stable
persistence id, and saves updated state as users change column layout.

## Dependencies

- Requires COR-031 table state core.
- Requires COR-032 table state stores.
- Requires COR-023 observable table model core.

## Scope

Add `TableStateController` under `corusco-swing.table`.

Required behavior:

- install against a `JTable`, `ObservableTableModel`, and `TableStateStore`;
- load and merge stored state with the current descriptor;
- apply column order, width bounds, visibility, and row-sorter sort keys;
- capture current column order, widths, visibility, and sort keys;
- save state after column model or sorter changes;
- expose a programmatic column visibility toggle for later header menu work;
- flush the store when closed.

## Required Deliverables

- Public Javadocs documenting EDT confinement, ownership, lifecycle, and
  immediate-save behavior.
- Unit tests for restore, persistence across table recreation, visibility
  toggling, sort capture/restore, and close flushing.
- Example update showing generated table bindings plus table-state controller
  usage with method-body comments.
- Stage-plan index update.

## Out of Scope

- Debounced save scheduling.
- Header popup menu UI.
- Preferences store integration examples.
- Schema-version migration hooks beyond existing descriptor merge semantics.

## Implementation Steps

1. Add this stage plan and index entry.
2. Implement the Swing table state controller and package docs.
3. Add focused EDT tests using `InMemoryTableStateStore`.
4. Update an example to demonstrate install/restore/save flow.
5. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- Stored column order, width, and visibility are restored on a new table.
- Unknown stored columns are ignored through the core merge algorithm.
- New descriptor columns remain available and are inserted by default order.
- Sort state is restored to a Swing row sorter when present.
- Controller saves current state after relevant table layout changes.
- Closing the controller removes listeners and flushes the store.
