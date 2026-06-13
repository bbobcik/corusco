# COR-029 Add Generated Column Persistence Metadata

Commit message:

```text
COR-029 Add generated column persistence metadata
```

## Roadmap Coverage

Roadmap Stage 13: Annotation Processor Phase 4, generated column persistence
metadata slice.

## Objective

Add explicit column persistence metadata to table descriptors so Stage 14 table
state can persist and restore width, order, visibility, and sort state with
stable ids and width clamp boundaries.

## Dependencies

- Requires COR-026 generated table column metadata.
- Requires COR-028 generated table resources/help metadata.

## Scope

Add core table metadata:

- `ColumnPersistence`

Extend `ColumnDescriptor` with optional persistence metadata while keeping
existing handwritten constructor calls source-compatible.

Extend `@Column` and generated table output:

- `persistenceId`, defaulting to the column key id;
- `minWidth`, defaulting to a conservative positive minimum;
- `maxWidth`, defaulting to `Integer.MAX_VALUE`;
- validation for stable persistence ids and `0 < minWidth <= width <= maxWidth`;
- generated descriptors that include `ColumnPersistence` construction.

## Required Deliverables

- Public Javadocs for new persistence metadata and updated descriptor/annotation
  contracts.
- Unit tests for metadata validation and descriptor construction.
- Processor tests proving generated persistence metadata and invalid width
  bounds diagnostics.
- Example updated to read generated persistence metadata with method-body
  comments explaining its role for later table-state persistence.
- Stage-plan index update.

## Out of Scope

- Table state stores.
- Preferences integration.
- Applying persisted state to `JTable`.
- Sort-state persistence.
- Table header visibility menu.

## Implementation Steps

1. Add this stage plan and index entry.
2. Add `ColumnPersistence` to `corusco-core`.
3. Extend `ColumnDescriptor` with persistence metadata and compatibility
   constructors.
4. Add `@Column` persistence attributes and processor validation.
5. Generate `ColumnPersistence.of(...)` in generated column descriptors.
6. Update tests/examples and run AudEnv compact test/build recommendations.

## Acceptance Checks

- Generated columns include stable persistence ids.
- Invalid persistence ids and width bounds fail compilation with clear
  diagnostics.
- Existing handwritten table code using old descriptor constructors still
  compiles.
- Generated editable columns, table resources/help, and Glazed Lists row-source
  compatibility continue to work.
