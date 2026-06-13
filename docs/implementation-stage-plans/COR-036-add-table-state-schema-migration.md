# COR-036 Add Table State Schema Migration

Commit message:

```text
COR-036 Add table state schema migration
```

## Roadmap Coverage

Roadmap Stage 14: Table State Persistence, schema version support and
migration hook slice.

## Objective

Add explicit application schema-version metadata to `TableState` and provide a
typed migration hook that can transform loaded state before descriptor merge.
This gives applications a safe place to handle persistence-id renames or other
table layout schema changes without weakening the existing descriptor-based
merge rules.

## Dependencies

- Requires COR-031 table state core.
- Requires COR-032 table state stores.
- Requires COR-033 table state controller.

## Scope

Add schema migration support:

- `TableState.schemaVersion()`;
- default schema version convenience constructor;
- schema-version persistence in `PreferencesTableStateStore`;
- `TableStateMigration<R>` hook with no-op and chaining helpers;
- `TableState.merge(..., migration)` overload;
- `TableStateController` overloads that accept a migration hook.

## Required Deliverables

- Public Javadocs explaining storage format version versus application schema
  version.
- Unit tests for schema validation, migration before merge, preference
  round-trip, and controller migration usage.
- Example update showing an old schema state migrated before descriptor merge
  with method-body comments.
- Stage-plan index update.

## Out of Scope

- Generated migration functions.
- A registry of named migrations.
- Changing current generated persistence ids.
- Cross-table migration orchestration.

## Implementation Steps

1. Add this stage plan and index entry.
2. Add schema version to `TableState` while preserving existing constructor
   ergonomics.
3. Add `TableStateMigration<R>` and merge overloads.
4. Persist schema version in preferences with backward-compatible read default.
5. Add controller overloads for migration-aware restore.
6. Update tests and examples.
7. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- Existing table-state construction still defaults to the current schema
  version.
- Explicit schema versions round-trip through memory and preferences stores.
- Invalid negative schema versions are rejected.
- Migration runs before descriptor merge and can rename old column ids.
- Controller restore can use the same migration hook.
