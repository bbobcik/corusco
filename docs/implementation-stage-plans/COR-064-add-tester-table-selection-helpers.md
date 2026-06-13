# COR-064 Add tester table selection helpers

## Commit Message

```text
COR-064 Add tester table selection helpers
```

## Roadmap Slice

Roadmap Stage 19: Swing MVP Test Harness, table selection helpers.

## Context

`COR-061` through `COR-063` established the tester shell, component lookup,
command invocation, and field input helpers. Stage 19 also needs concise table
selection helpers so tests can drive generated table views in model-row or
view-row coordinates without open-coded EDT blocks and Swing index conversion
logic in every test.

## Scope

- Add table view-row selection by generated table component key.
- Add table model-row selection with view/model conversion.
- Add table selection clearing.
- Add selected view/model row assertions.
- Add tests and an example with method-body comments.

## Out Of Scope

- Table cell editing helpers.
- Table-state assertions.
- Problem assertions for table cells.
- Behavior-installed assertions.
- Generated-source compiler helpers.

## Implementation Steps

1. Add `selectTableViewRow`, `selectTableModelRow`, and `clearTableSelection`.
2. Add selected view/model row assertion helpers.
3. Cover sorted-table conversion, invalid indexes, missing components, clear
   selection, EDT execution, and assertion diagnostics.
4. Add a focused example that selects rows using generated table component
   keys.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- Table selection helpers execute on the EDT.
- Model-row selection works when a table row sorter is active.
- Invalid indexes and missing table keys fail clearly.
- Assertions report generated component keys.
- No reflection, JavaBeans, or property-path APIs are introduced.
