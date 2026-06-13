# COR-051 Add Swing task callbacks

## Commit Message

```text
COR-051 Add Swing task callbacks
```

## Roadmap Slice

Roadmap Stage 17: Async Tasks and Async Validation, Swing EDT callback delivery
slice.

## Context

`COR-050` added a Swing-free task service with an injected callback executor.
The next roadmap requirement is EDT-delivered callbacks. This should be a small
Swing adapter around the core executor boundary, keeping task execution and
cancellation policy in `corusco-core`.

## Scope

- Add an EDT executor helper.
- Add `SwingTaskServices` factory helpers.
- Create virtual-thread-backed task services whose terminal callbacks and final
  busy-state updates run on the Swing EDT.
- Add tests for success callbacks, failure callbacks, and task busy completion
  events on the EDT.
- Add a focused example with method-body comments explaining task/callback
  ownership.

## Out Of Scope

- Async validation APIs.
- Stale-result generation helper.
- Busy overlay behavior.
- Command/action integration.
- Generated async bindings.

## Implementation Steps

1. Extend `SwingEdt` with an EDT executor.
2. Add `SwingTaskServices` in `corusco-swing`.
3. Add Swing tests proving callback and completion delivery on the EDT.
4. Add an example using the Swing task factory.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- Submitted task bodies run off the EDT.
- Success callbacks run on the EDT.
- Failure callbacks run on the EDT.
- Final task busy events run on the EDT.
- The helper uses core `TaskService` without duplicating task policy.
- No reflection, JavaBeans, or property-path APIs are introduced.
