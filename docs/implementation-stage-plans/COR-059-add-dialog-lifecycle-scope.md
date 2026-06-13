# COR-059 Add dialog lifecycle scope

## Commit Message

```text
COR-059 Add dialog lifecycle scope
```

## Roadmap Slice

Roadmap Stage 18: Modal Dialog Framework, dialog lifecycle integration with
presenters, behaviors, and tasks.

## Context

`COR-055` through `COR-058` established form dialog result flow, command
handling, dirty cancellation, keyboard/default-button wiring, and validation
summary/focus behavior. Stage 18 still needs lifecycle integration so a modal
dialog can own behavior bindings, presenter detachables, and task services and
release them deterministically when the dialog closes.

## Scope

- Add `FormDialogLifecycle` in the Swing dialog package.
- Own a `FormDialog` plus dialog-scoped `Binding`, `Disposable`, and
  `Detachable` resources.
- Close owned resources and the dialog deterministically and idempotently.
- Fail closed when resources are registered after lifecycle close.
- Add tests and an example with method-body comments.

## Out Of Scope

- Native `JDialog` factory/display helpers.
- Automatic presenter discovery.
- Generated behavior-plan emission.
- Async task cancellation policies beyond closing registered disposables.

## Implementation Steps

1. Add lifecycle owner API with registration methods for bindings,
   disposables, detachables, and task services.
2. Add tests for close order, idempotency, late registration, detachables, task
   service cleanup, and repeated lifecycle creation.
3. Add a focused example showing dialog binding and detachable ownership.
4. Run test/build checks and commit the slice.

## Acceptance Checks

- Closing a lifecycle closes registered resources and then closes the dialog.
- Closing is idempotent.
- Late resource registration closes/detaches immediately.
- Dialog lifecycles can be created and closed repeatedly without retained
  registrations.
- No reflection, JavaBeans, or property-path APIs are introduced.
