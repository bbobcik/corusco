# COR-048 Add detachable lifecycle scope

## Commit Message

```text
COR-048 Add detachable lifecycle scope
```

## Roadmap Slice

Roadmap Stage 16: Detachable and Loadable Models, attach/detach lifecycle
integration slice.

## Context

`COR-046` and `COR-047` added detachable values and lists, but presenters still
need a coherent lifecycle owner for those models. Final disposal and temporary
detachment are different operations: `SubscriptionScope` closes listener
registrations permanently, while detachable models should usually remain
registered and reusable across repeated view activation cycles.

## Scope

- Add `DetachableScope`.
- Allow presenters to register detachable children.
- Detach children in reverse registration order.
- Keep children registered after `detach()` so repeated view deactivation is
  possible.
- Add `close()` to detach once more, clear references, and reject late
  registrations by detaching them immediately.
- Aggregate detach failures while still visiting every child.
- Add tests and an example with method-body comments.

## Out Of Scope

- Swing `BindingScope` or `BehaviorScope` integration.
- Master-detail loadable helpers.
- Async load lifecycle.
- Generated presenter lifecycle code.

## Implementation Steps

1. Add `DetachmentException`.
2. Add `DetachableScope` in the lifecycle package.
3. Extend `Detachable` with a no-op constant for tests/examples.
4. Add lifecycle tests for ordering, repeat detach, close, late registration,
   and failure aggregation.
5. Add a focused example using `LoadableValue`, `LoadableList`, and
   `DetachableScope`.
6. Run test/build checks and commit the slice.

## Acceptance Checks

- A presenter can register detachable values/lists in one scope.
- `detach()` releases all registered caches without permanently closing the
  scope.
- `close()` detaches and clears registrations.
- Late registration after close detaches immediately.
- Failure in one child does not prevent detaching the rest.
- No Swing, reflection, JavaBeans, or property-path APIs are introduced.
