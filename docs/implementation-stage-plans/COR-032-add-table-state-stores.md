# COR-032 Add Table State Stores

Commit message:

```text
COR-032 Add table state stores
```

## Roadmap Coverage

Roadmap Stage 14: Table State Persistence, store abstraction and concrete
core store implementations slice.

## Objective

Introduce a Swing-free persistence boundary for `TableState` so later table
controllers can load, save, remove, and flush state without knowing whether the
backing store is memory, preferences, or an application-specific adapter.

## Dependencies

- Requires COR-031 table state core.

## Scope

Add core table state store types:

- `TableStateStore`
- `TableStateStoreException`
- `InMemoryTableStateStore`
- `PreferencesTableStateStore`

Required behavior:

- load optional table state by stable table id;
- save immutable table state snapshots;
- remove stored state by table id;
- flush pending durable-store writes;
- preserve columns and sort entries round-trip;
- report corrupt or inaccessible preference data with a clear store exception.

## Required Deliverables

- Public Javadocs documenting ownership, durability, flush behavior, and
  exception boundaries.
- Unit tests covering in-memory round-trip, defensive snapshots, preferences
  round-trip, remove, flush, and corrupt preferences data.
- Example showing table state merge plus store save/load flow with method-body
  comments.
- Stage-plan index update.

## Out of Scope

- Applying state to `JTable`.
- Debounced save behavior.
- Controller lifecycle integration.
- User-facing column visibility menus.
- Schema-version migration hooks beyond rejecting malformed persisted records.

## Implementation Steps

1. Add this stage plan and index entry.
2. Add the store API and unchecked store exception under `corusco-core.table`.
3. Add in-memory and preferences-backed store implementations.
4. Add focused unit tests for both implementations and error handling.
5. Update the table-state example to demonstrate store usage.
6. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- Stores round-trip `TableState` without losing columns or sort state.
- Stored snapshots are isolated from subsequent caller references.
- Removing a table id makes future loads empty.
- Preferences-backed state survives a new store instance over the same node.
- Malformed preference records fail with `TableStateStoreException`.
- Store code remains Swing-free and reflection-free.
