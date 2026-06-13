# COR-055 Add dialog result and form controller

## Commit Message

```text
COR-055 Add dialog result and form controller
```

## Roadmap Slice

Roadmap Stage 18: Modal Dialog Framework, foundational dialog result and form
controller slice.

## Context

Stage 17 finished async task and busy-overlay support. Stage 18 starts modal
form dialogs. The first coherent step should establish terminal dialog results
and a headless-testable controller that coordinates form commit/cancel/apply
logic without depending on native dialog display.

## Scope

- Add `DialogResult<R>` sealed result hierarchy in core.
- Add Swing `FormDialog<P, R>` controller for a `FormModel<R>` and root
  component.
- Expose OK, Apply, and Cancel commands backed by existing command APIs.
- Commit the active Swing editor before OK/Apply.
- Block OK/Apply when editor commit fails or the form is not committable.
- Mark accepted/applied values as the new form baseline.
- Add tests and an example with method-body comments.

## Out Of Scope

- Native `JDialog` construction/display helpers.
- Dirty-cancel confirmation hook.
- ESC/default-button key installation.
- Validation summary and focus-first-problem behavior.
- Presenter/task lifecycle integration.

## Implementation Steps

1. Add core dialog package and `DialogResult<R>`.
2. Add Swing dialog package and `FormDialog<P, R>`.
3. Add tests for accepted/cancelled results, command enablement, editor commit
   failure, committability blocking, apply without close, and close cleanup.
4. Add a focused example demonstrating OK, Apply, and Cancel semantics.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- OK creates an accepted result only after editor commit succeeds and the form
  is committable.
- Cancel produces a cancelled result and does not call `toResult()`.
- Apply commits and accepts current values without closing the controller.
- Commands use stable action/resource keys and existing command APIs.
- Controller lifecycle is EDT-bound and idempotent.
- No reflection, JavaBeans, or property-path APIs are introduced.
