# COR-030 Add Generated Table Bindings

Commit message:

```text
COR-030 Add generated table bindings
```

## Roadmap Coverage

Roadmap Stage 13: Annotation Processor Phase 4, generated table binding helper
slice.

## Objective

Generate table binding helper classes that connect generated table descriptors
to existing Swing table infrastructure. This completes the Stage 13 generated
type family by adding `CustomerRowTableBindings` beside generated columns,
resources, and descriptors.

## Dependencies

- Requires COR-023 observable table model core.
- Requires COR-024 table selection binding.
- Requires COR-026 generated table column metadata.

## Scope

Generated type for `CustomerRow`:

- `CustomerRowTableBindings`

Required behavior:

- create and install the generated `ObservableTableModel<R>` into a supplied
  `JTable`;
- register the generated table model in a supplied `BindingScope` so lifecycle
  cleanup is deterministic;
- expose overloads for generated selection binding by model-row index and by
  model-row index plus selected row value;
- delegate to existing `TableSelectionBinding` so view/model index conversion
  and sorter behavior stay centralized in runtime code.

## Required Deliverables

- Dedicated table binding source generation using text blocks.
- Processor tests proving generated binding helper source.
- Example updated to use generated table bindings with method-body comments for
  model installation, selection binding, and lifecycle ownership.
- Stage-plan index update.

## Out of Scope

- New runtime binding primitives.
- Table state persistence or applying persisted state to `JTable`.
- Multi-row selection.
- Generated table cell validation binding.

## Implementation Steps

1. Add this stage plan and index entry.
2. Extend `TableSourceWriter` to emit `<Row>TableBindings`.
3. Add generated helper methods for model installation and selection binding.
4. Update processor tests and the generated table example.
5. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- Generated table binding helpers compile without reflection.
- Installing a generated model sets the supplied `JTable` model and registers
  cleanup in `BindingScope`.
- Generated selection helpers bind through `TableSelectionBinding` and update
  selected row values.
- Existing generated table resources, persistence metadata, editable columns,
  and Glazed Lists row-source compatibility continue to work.
