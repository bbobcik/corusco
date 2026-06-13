# COR-022 Add EDT Observable List Dispatcher

Commit message:

```text
COR-022 Add EDT observable list dispatcher
```

## Roadmap Coverage

Roadmap Stage 11: Observable Collections and List Adapters, explicit EDT
dispatch slice.

## Objective

Add a Swing-side observable-list wrapper that marshals source-list change
delivery to the Swing Event Dispatch Thread. This completes the Stage 11
threading path without changing the synchronous, Swing-free contract of
`corusco-core` collections.

## Dependencies

- Requires COR-017 observable list core.
- Requires COR-019 Swing list adapters and EDT helper conventions.

## Scope

Add public API in `corusco-swing`:

- `EdtObservableList<E>`

Required behavior:

- wrap any `ObservableList<E>`;
- delegate reads and mutations to the wrapped source list;
- deliver wrapper subscriber callbacks on the EDT;
- preserve each source `ListChangeSet` as one delivered change set;
- deliver synchronously when the source already fires on the EDT;
- deliver with `SwingUtilities.invokeLater` when the source fires off the EDT;
- close deterministically by removing the source subscription and suppressing
  queued-but-not-yet-delivered events;
- document that this wrapper marshals notifications only and does not make the
  wrapped source list itself thread-safe.

## Required Deliverables

- `EdtObservableList` implementation with Javadocs for threading, lifecycle,
  ownership, and queued delivery.
- Swing collection tests proving EDT delivery from background source mutations,
  same-EDT synchronous delivery, close behavior, and integration with
  `ObservableListModel`.
- Example demonstrating background-originated list changes flowing to EDT
  subscribers, with method-body comments explaining source ownership, queueing,
  and cleanup.
- Stage-plan index update.

## Out of Scope

- Making core observable lists synchronized.
- Blocking background callers until EDT delivery completes.
- Coalescing multiple source change sets into one EDT event.
- Replacing Swing adapters' fail-fast behavior when used without this wrapper.
- Table-model threading; table infrastructure starts in Stage 12.

## Implementation Steps

1. Add the COR-022 stage plan and index entry.
2. Implement `EdtObservableList` in `corusco-swing`.
3. Add focused tests around threading and lifecycle.
4. Add a small example and example test.
5. Run AudEnv compact test/build recommendations and critical review scans.

## Acceptance Checks

- Existing `ObservableListModel` continues to fail fast for off-EDT source
  changes when no dispatcher is used.
- Wrapping a source list with `EdtObservableList` allows background-originated
  source changes to reach Swing list models on the EDT.
- Closing the wrapper removes its source subscription and prevents queued events
  from being delivered later.
