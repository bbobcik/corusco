# COR-050 Add async task core

## Commit Message

```text
COR-050 Add async task core
```

## Roadmap Slice

Roadmap Stage 17: Async Tasks and Async Validation, core task service slice.

## Context

Stage 17 starts with a task service, virtual-thread-backed default
implementation, busy state, cancellation, and callback delivery. The first
commit should be Swing-free but must leave a clean callback executor seam so
`corusco-swing` can deliver callbacks on the EDT in the next slice.

## Scope

- Add core task package APIs:
  - `CancellationToken`
  - `CancellationSource`
  - `TaskCancelledException`
  - `UiTask<T>`
  - `TaskCallbacks<T>`
  - `TaskHandle<T>`
  - `TaskService`
  - `DefaultTaskService`
- Run submitted tasks off the caller thread.
- Provide a virtual-thread-backed factory.
- Maintain task-level and service-level busy values.
- Cancel running tasks and suppress success/failure callbacks after
  cancellation.
- Deliver callbacks through an injected callback executor.
- Add tests and a focused example.

## Out Of Scope

- Swing EDT callback factory.
- Async validation APIs.
- Stale-result generation helper.
- Busy overlay behavior.
- Generated async bindings.

## Implementation Steps

1. Add task package public APIs with lifecycle/cancellation Javadocs.
2. Implement `DefaultTaskService` with virtual-thread and executor-backed
   factories.
3. Add tests for background execution, virtual threads, callback executor,
   busy values, cancellation, failure handling, and service close behavior.
4. Add a small example with method-body comments explaining callback ownership.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- Blocking task runs off the caller thread.
- Default factory uses virtual threads.
- Success/failure/cancellation callbacks are delivered through the configured
  callback executor.
- Task and service busy values update deterministically.
- Cancelling a task prevents success/failure callbacks.
- Closing the service cancels running tasks and rejects new submissions.
- No Swing, reflection, JavaBeans, or property-path APIs are introduced.
