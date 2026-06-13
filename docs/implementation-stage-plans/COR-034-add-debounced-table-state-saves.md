# COR-034 Add Debounced Table State Saves

Commit message:

```text
COR-034 Add debounced table state saves
```

## Roadmap Coverage

Roadmap Stage 14: Table State Persistence, debounced save/flush behavior slice.

## Objective

Change Swing table state persistence from one store write per column/sorter
event to coalesced EDT saves. This prevents column drag/resize gestures from
producing excessive persistence writes while preserving deterministic flush on
view disposal.

## Dependencies

- Requires COR-033 table state controller.

## Scope

Add debounced save scheduling to `TableStateController`:

- default delayed saves for column model and sorter events;
- explicit immediate-save path for callers that need it;
- pending-save flush hook;
- guaranteed pending-save flush before store flush on controller close;
- focused scheduler helper to keep timer mechanics out of the controller.

## Required Deliverables

- Public Javadocs documenting delayed event saves, immediate save, and close
  flush semantics.
- Unit tests proving event saves are coalesced, explicit saves remain
  immediate, and close persists pending changes before flushing the store.
- Example comments that explain lifecycle-owned flush behavior.
- Stage-plan index update.

## Out of Scope

- Header popup menu UI.
- Preferences-backed demo.
- Schema-version migration hooks.
- Cross-thread scheduling; controller and scheduler remain EDT-confined.

## Implementation Steps

1. Add this stage plan and index entry.
2. Add a package-private EDT save scheduler using a one-shot Swing timer.
3. Update `TableStateController` to schedule event saves and flush pending work
   on close.
4. Add/update controller tests for debounce behavior.
5. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- Column and sorter events schedule delayed saves instead of saving
  immediately.
- Multiple changes inside one debounce interval produce one store save.
- `saveNow()` persists immediately and cancels pending delayed saves.
- `flushPendingSaves()` writes pending state deterministically.
- `close()` writes pending state and then flushes the backing store.
