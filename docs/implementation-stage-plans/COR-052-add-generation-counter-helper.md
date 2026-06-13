# COR-052 Add generation counter helper

## Commit Message

```text
COR-052 Add generation counter helper
```

## Roadmap Slice

Roadmap Stage 17: Async Tasks and Async Validation, stale-result generation
counter helper slice.

## Context

Async validation and background lookups need to ignore results that arrive
after the user has changed the relevant field. `COR-050` and `COR-051` provide
task execution and EDT callback delivery; the next small foundation is a
thread-safe generation helper that callbacks can use to decide whether a result
is still current.

## Scope

- Add `GenerationCounter` in the core task package.
- Provide immutable generation tokens.
- Advance generations when scheduling or invalidating work.
- Check whether a captured generation is current or stale.
- Provide a small `tryAccept(...)` helper for applying a result only when
  current.
- Add tests and an example with method-body comments.

## Out Of Scope

- Async validation APIs.
- Field model integration.
- Task cancellation integration.
- Swing bindings or generated plans.

## Implementation Steps

1. Add `GenerationCounter`.
2. Add focused tests for current/stale checks, invalidation, callback helpers,
   null results, and thread-visible generation changes.
3. Add an example showing out-of-order async validation result suppression.
4. Run test/build checks and commit the slice.

## Acceptance Checks

- Captured generations can be compared against the current generation.
- Advancing or invalidating makes older generations stale.
- `tryAccept(...)` applies only current results.
- Null result values can still be applied when current.
- The helper is thread-safe for worker/callback handoff.
- No Swing, reflection, JavaBeans, or property-path APIs are introduced.
