# COR-025 Add Table Cell Problem Decoration

Commit message:

```text
COR-025 Add table cell problem decoration
```

## Roadmap Coverage

Roadmap Stage 12: Table Model and Table Column Descriptors, table cell problem
target and validation decoration placeholder slice.

## Objective

Connect the existing problem model to typed table columns. A table cell problem
should target a row object plus `ColumnKey<R, V>`, and Swing tables should have
an explicit decoration binding that can surface those problems without
JavaBeans property paths or arbitrary column-name strings.

## Dependencies

- Requires COR-005 problem model.
- Requires COR-023 observable table model core.

## Scope

Add core API:

- `TableCellProblems`

Add Swing API:

- `TableCellValidationBinding`

Required behavior:

- create `ProblemTarget.Cell<R, ColumnKey<R, V>>` values from rows and typed
  column keys;
- filter `ProblemSet` values by exact row and typed column key;
- install a Swing table-cell renderer wrapper that marks cells with matching
  problems;
- use JTable view/model conversion for sorted tables;
- repaint on problem-set changes;
- remove subscriptions and restore original renderers on close;
- document that row identity uses the row object's `equals` contract.

## Required Deliverables

- Core helper and Swing binding production code with Javadocs.
- Tests for typed cell target/filter behavior.
- Swing tests for decoration, sorted table conversion, problem changes, and
  renderer restoration on close.
- Example showing cell problem decoration with method-body comments explaining
  row identity, column-key targeting, and cleanup.
- Stage-plan index update.

## Out of Scope

- Generated validators for table rows.
- Custom renderers beyond wrapping the table's current default renderers.
- Persisting problem decorations.
- Multi-problem popovers or rich cell editors.

## Implementation Steps

1. Add this stage plan and index entry.
2. Add `TableCellProblems` in `corusco-core`.
3. Add `TableCellValidationBinding` in `corusco-swing`.
4. Add core and Swing tests.
5. Add an example and smoke test.
6. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- Cell problem targeting uses typed `ColumnKey`, not column-name strings.
- Decorated Swing cells are found correctly through view/model conversion.
- Closing the binding restores original renderers and stops repaint updates.
