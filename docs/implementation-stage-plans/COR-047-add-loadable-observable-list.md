# COR-047 Add loadable observable list

## Commit Message

```text
COR-047 Add loadable observable list
```

## Roadmap Slice

Roadmap Stage 16: Detachable and Loadable Models, loadable observable-list
slice.

## Context

`COR-046` introduced detachable lazy values. Stage 16 also calls for a
loadable list or adapter to `ObservableList<E>` so table/list presenters can
hold a lazy row source and release cached rows when inactive. This commit keeps
the feature Swing-free and uses the existing observable-list change model.

## Scope

- Add supplier-backed `LoadableList<E>`.
- Implement the existing mutable `ObservableList<E>` contract.
- Load rows lazily on first list access or mutation.
- Detach/invalidate the cached list without removing external subscribers.
- Refresh the cached list and emit a reset-style change set when the loaded
  snapshot changes.
- Add tests for lazy load, mutation events, refresh events, subscription
  cleanup, and practical cache garbage collection after detach.
- Add an example with method-body comments explaining presenter ownership.

## Out Of Scope

- Swing list/table adapter changes.
- Glazed Lists specific adapters.
- Async/background loading.
- Master-detail list helpers.
- Diff-minimizing refresh algorithms.

## Implementation Steps

1. Add `LoadableList<E>` in the core collection package.
2. Forward existing observable-list mutations through the attached cache.
3. Add detach/invalidate/refresh lifecycle methods.
4. Add unit tests and a focused example.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- First list access loads lazily.
- Repeated access reuses the attached cache.
- Mutations fire ordinary observable-list changes.
- `detach()` releases cached rows and preserves subscribers.
- `refresh()` emits clear/insert reset changes only when the loaded snapshot
  differs.
- Cached rows can be garbage collected after detach where practical.
- No Swing, reflection, JavaBeans, or property-path APIs are introduced.
