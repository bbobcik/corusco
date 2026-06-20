# COR-026 Add Generated Table Columns

Commit message:

```text
COR-026 Add generated table columns
```

## Roadmap Coverage

Roadmap Stage 13: Annotation Processor Phase 4, generated table column metadata
foundation.

## Objective

Generate typed table column metadata from annotated row records. This stage
adds the annotation and processor path for read-only record rows, targeting the
table APIs introduced in Stage 12 without reflection or JavaBeans property
paths.

## Dependencies

- Requires COR-018 processor source-template refactor.
- Requires COR-023 observable table model core.

## Scope

Add annotations:

- `@CoruscoTable`
- `@Column`

Generated types for `CustomerRow`:

- `CustomerRowColumns`
- `CustomerRowTableDescriptor`

Required behavior:

- generate stable `TableKey` and `ColumnKey` constants;
- generate header and optional tooltip `ResourceKey<String>` constants;
- generate `ColumnDescriptor` values with defaults and capabilities;
- generate read-only `Column<R, V>` constants using record accessor method
  references;
- generate an ordered `TableDescriptor<R>`;
- generate an `ObservableTableModel<R>` factory;
- accept any `ObservableList<R>` row source, including the optional first-class
  `GlazedObservableList<R>` adapter around mature Glazed Lists `EventList`
  pipelines;
- reject unsupported generic table records, duplicate column ids, invalid ids,
  invalid widths, and editable columns for this slice with an explicit
  diagnostic;
- box primitive value types in generated `ColumnKey` and `ColumnDescriptor`
  type tokens.

## Required Deliverables

- New annotation types with Javadocs.
- Modular processor table spec/writer code separate from form source writing.
- Processor tests for generated source and validation errors.
- Example row record using generated table metadata, with method-body comments
  explaining generated keys/descriptors, Glazed Lists row adaptation, and
  read-only deferral.
- Stage-plan index update.

## Out of Scope

- Generated editable row updater/wither support.
- Generated table bindings.
- Generated table resource bundle files.
- Table state persistence.

## Implementation Steps

1. Add this stage plan and index entry.
2. Add table annotations.
3. Add table spec extraction and validation to the processor.
4. Add a dedicated table source writer.
5. Add processor tests and an example using generated table metadata.
6. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- Generated columns use typed `ColumnKey` constants and accessor method
  references, not string property paths.
- Generated table descriptor can create `ObservableTableModel`.
- Generated table descriptor works with a `GlazedObservableList<R>` anywhere an
  `ObservableList<R>` is expected.
- Editable generated row updater support is explicitly deferred with a
  compiler diagnostic.
