# COR-024 Add Table Selection Binding

Commit message:

```text
COR-024 Add table selection binding
```

## Roadmap Coverage

Roadmap Stage 12: Table Model and Table Column Descriptors, table selection
binding slice.

## Objective

Add an explicit Swing table selection binding that converts between `JTable`
view rows and `ObservableTableModel` model rows. Presenters should be able to
observe and drive table selection through typed Corusco values without using
Swing selection APIs directly.

## Dependencies

- Requires COR-003 observable value primitives.
- Requires COR-023 observable table model core.

## Scope

Add public Swing API:

- `TableSelectionBinding<R>`

Required behavior:

- bind a `JTable` and `ObservableTableModel<R>` to caller-owned
  `WritableValue<Integer>` selected model-row index;
- optionally bind to caller-owned `WritableValue<R>` selected row value;
- convert JTable view indices to model indices with `JTable` conversion APIs;
- allow presenter-originated selection by setting the bound model-row value;
- clear selection values when there is no selection or the selected model row
  no longer exists;
- refresh selection after table model changes and active row sorter changes;
- document that sorter replacement requires closing and recreating the binding;
- remove all Swing and value listeners on close;
- document EDT, lifecycle, equality, and ownership expectations.

## Required Deliverables

- `TableSelectionBinding` production implementation.
- Unit tests for user selection, presenter-driven selection, sorter conversion,
  row removal, close behavior, and EDT failures.
- Example showing a sorted table whose selected model row and selected row
  value are observed through Corusco values, with method-body comments for
  view/model index conversion and cleanup.
- Stage-plan index update.

## Out of Scope

- Multi-row selection.
- Selection by stable row key.
- Table cell problem decorations.
- Persisting table selection.

## Implementation Steps

1. Add this stage plan and index entry.
2. Implement `TableSelectionBinding` in `corusco-swing`.
3. Add focused Swing tests with sorting and row removal.
4. Add a table selection example and smoke test.
5. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- Selection binding converts view row indices to model row indices correctly.
- Presenter-driven model-row selection selects the corresponding view row.
- Closing the binding removes installed listeners.
