# COR-049 Add master-detail loadable value

## Commit Message

```text
COR-049 Add master-detail loadable value
```

## Roadmap Slice

Roadmap Stage 16: Detachable and Loadable Models, master-detail loadable value
helper slice.

## Context

Stage 16 calls out master-detail loadable value helpers. After `LoadableValue`,
`LoadableList`, and `DetachableScope`, the missing primitive is a lazy detail
value that follows a selected master value. Active details should update when
the master changes; detached details should remain cheap and load only when the
view asks for them again.

## Scope

- Add `MasterDetailValue<M, T>`.
- Subscribe to a master `ReadableValue<M>`.
- Load details lazily for the current master.
- When attached, reload and notify on master changes.
- When detached, invalidate on master changes without eager loading.
- Support `detach()`, `invalidate()`, `refresh()`, and `close()`.
- Add tests for lazy loading, active master changes, detached master changes,
  refresh, subscription cleanup, and close behavior.
- Add an example with method-body comments explaining the active/detached
  master-detail lifecycle.

## Out Of Scope

- Master-detail list helpers.
- Async/background detail loading.
- Swing selection binding integration.
- Generated presenter helper emission.

## Implementation Steps

1. Add `MasterDetailValue<M, T>` in the value package.
2. Subscribe to the master value and implement active/detached reload behavior.
3. Add unit tests and a focused example.
4. Run test/build checks and commit the slice.

## Acceptance Checks

- Detail loading is lazy until `value()` or `refresh()` is called.
- Repeated `value()` calls reuse the attached detail for the same master.
- Active master changes reload detail and emit an event when changed.
- Detached master changes do not load detail eagerly.
- `refresh()` reloads the current master's detail.
- `close()` removes the master subscription and clears listeners.
- No Swing, reflection, JavaBeans, or property-path APIs are introduced.
