# COR-046 Add detachable value core

## Commit Message

```text
COR-046 Add detachable value core
```

## Roadmap Slice

Roadmap Stage 16: Detachable and Loadable Models, core detachable value
foundation slice.

## Context

Stage 16 introduces Wicket-inspired detachable model lifecycles for Swing
presenters that temporarily need expensive data. The first commit should be
Swing-free and small: define the detach contract and a lazy value
implementation that releases cached data on detach while preserving the
existing synchronous `ReadableValue` conventions.

## Scope

- Add a core `Detachable` lifecycle contract.
- Add a `DetachableValue<T>` contract for lazy readable values that can release
  cached data.
- Add `LoadableValue<T>` with supplier-backed lazy loading.
- Add refresh/invalidate semantics.
- Notify subscribers when a reload changes the effective value.
- Add tests proving lazy load, cache reuse, detach release, refresh reload,
  subscription behavior, and practical garbage collection after detach.
- Add an example with method-body comments explaining expensive-load ownership.

## Out Of Scope

- Detachable observable-list adapters.
- Swing presenter/view lifecycle integration.
- Master-detail helper APIs.
- Async loading or background task execution.
- Generated model integration.

## Implementation Steps

1. Add `Detachable` and `DetachableValue<T>` APIs.
2. Implement supplier-backed `LoadableValue<T>`.
3. Add unit tests for lifecycle, refresh, events, and cache release.
4. Add a focused example using `LoadableValue`.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- Expensive value loads lazily on first `value()` access.
- Repeated `value()` calls reuse the attached cache.
- `detach()` releases cached data and the next `value()` reloads.
- `invalidate()` releases cached data without eager reload.
- `refresh()` reloads and emits a value event only when the effective value
  changes.
- Closing subscriptions stops future notifications.
- Cached data can be garbage collected after detach where practical.
- No Swing, reflection, JavaBeans, or property-path APIs are introduced.
