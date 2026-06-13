# COR-031 Add Table State Core

Commit message:

```text
COR-031 Add table state core
```

## Roadmap Coverage

Roadmap Stage 14: Table State Persistence, immutable state model and merge
algorithm slice.

## Objective

Introduce Swing-free table state metadata that can represent persisted column
layout and sort state, then merge stored state with a current generated table
descriptor. This gives later store/controller commits a deterministic core
contract for migration, new columns, unknown old columns, and width clamping.

## Dependencies

- Requires COR-023 observable table model core.
- Requires COR-029 generated column persistence metadata.

## Scope

Add core table state types:

- `TableState`
- `ColumnState`
- `SortState`
- `SortDirection`

Required behavior:

- create default state from a `TableDescriptor`;
- merge an optional stored state with the current descriptor;
- ignore unknown old columns;
- add new columns according to descriptor default order;
- clamp stored widths to each column persistence min/max bounds;
- preserve stored visibility and order for known columns;
- preserve sort state only for columns that still exist.

## Required Deliverables

- Public Javadocs documenting immutability, descriptor merge semantics, and
  persistence boundaries.
- Unit tests for default state, stored-state merge, unknown old columns, new
  columns, width clamping, and sort filtering.
- Example showing descriptor/default-state/merged-state flow with method-body
  comments.
- Stage-plan index update.

## Out of Scope

- Table state stores.
- Preferences integration.
- Applying state to `JTable`.
- Debounced save/flush behavior.
- Schema-version migration hooks beyond deterministic descriptor merge.

## Implementation Steps

1. Add this stage plan and index entry.
2. Add immutable state records under `corusco-core.table`.
3. Implement default-state creation and descriptor merge logic.
4. Add unit tests covering merge edge cases.
5. Add/update an example that demonstrates state merge without Swing.
6. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- Default table state follows descriptor order, width, and visibility.
- Stored state for known columns is preserved and widths are clamped.
- Unknown stored columns are ignored.
- New descriptor columns are inserted by default order.
- Sort state survives only for known columns.
- Core table state code remains Swing-free and reflection-free.
