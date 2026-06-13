# COR-027 Add Generated Table Row Updaters

Commit message:

```text
COR-027 Add generated table row updaters
```

## Roadmap Coverage

Roadmap Stage 13: Annotation Processor Phase 4, editable generated table
column metadata slice.

## Objective

Allow generated table columns to be editable when the annotated row type is a
non-generic record. The processor should generate explicit record-constructor
row replacement helpers and wire them into `Column.editable`, avoiding
reflection, JavaBeans property paths, and handwritten wither methods.

## Dependencies

- Requires COR-023 observable table model core.
- Requires COR-026 generated table column metadata.

## Scope

Extend `@Column(editable = true)` support for record components:

- generate editable `Column<R, V>` constants for editable components;
- generate private row updater helper methods that call the record constructor
  with the edited value and current values from all other record components;
- generate `ColumnCapabilities(..., true, ...)` for editable columns;
- preserve boxed primitive value type tokens for `JTable.getColumnClass()` and
  updater casts;
- keep generated source in the dedicated table source writer using text blocks.

## Required Deliverables

- Annotation Javadocs updated to describe generated updater support.
- Processor spec/writer updates for editable column metadata.
- Processor tests proving editable generated source and table-model editing.
- Example updated to edit a generated record-row table column with method-body
  comments explaining record replacement and Glazed Lists row-source
  compatibility.
- Stage-plan index update.

## Out of Scope

- Generated table resource bundle files.
- Generated table binding plans.
- Table state persistence.
- Custom wither method discovery.
- Editable columns for non-record or generic row types.

## Implementation Steps

1. Add this stage plan and index entry.
2. Extend table spec extraction to retain all record component names and
   editable column flags.
3. Generate constructor-based updater helpers for editable columns.
4. Update validation so `@Column(editable = true)` is accepted for supported
   record component types.
5. Add processor tests and update the generated-table example.
6. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- `@Column(editable = true)` compiles for supported record component types.
- Generated editable columns use `Column.editable` with an explicit generated
  updater helper.
- Editing through `ObservableTableModel.setValueAt` replaces the row record in
  the source list.
- Generated code still contains no reflection or string property paths.
- Generated table descriptors continue to accept `GlazedObservableList<R>` as
  an ordinary `ObservableList<R>` row source.
