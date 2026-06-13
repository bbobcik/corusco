# COR-056 Add dirty-cancel confirmation

## Commit Message

```text
COR-056 Add dirty-cancel confirmation
```

## Roadmap Slice

Roadmap Stage 18: Modal Dialog Framework, dirty-cancel confirmation hook slice.

## Context

`COR-055` introduced dialog results and a headless-testable `FormDialog`
controller. Stage 18 next requires cancel handling that can ask for
confirmation when the form has unsaved changes. The existing `FormModel`
contract intentionally has no aggregate dirty flag, so this slice should add
explicit dirty-state and confirmation hooks instead of inspecting fields by
reflection or property paths.

## Scope

- Add a Swing dialog `DirtyState` functional interface.
- Add a Swing dialog `CancelConfirmation` functional interface.
- Extend `FormDialog` with constructors that accept dirty-state and
  confirmation hooks.
- Make Cancel ask confirmation only when dirty.
- Reset the form only after cancellation is confirmed.
- Preserve idempotent close cleanup while keeping user cancel confirmable.
- Add tests and an example with method-body comments.

## Out Of Scope

- Native `JOptionPane` confirmation UI.
- ESC/default-button key installation.
- Validation summary and focus-first-problem behavior.
- Form-level dirty aggregation in core.
- Generated behavior-plan emission.

## Implementation Steps

1. Add `DirtyState` and `CancelConfirmation` in the Swing dialog package.
2. Extend `FormDialog` constructors and cancel semantics.
3. Add tests for clean cancel, dirty confirm, dirty reject, command execution,
   close cleanup, and idempotency.
4. Update/add an example showing clean and dirty cancel flows.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- Clean cancel closes without asking confirmation.
- Dirty cancel asks confirmation.
- Rejected confirmation keeps the controller open and leaves the form unchanged.
- Confirmed cancellation resets the form and returns a cancelled result.
- Close remains deterministic cleanup and does not depend on user
  confirmation.
- No reflection, JavaBeans, or property-path APIs are introduced.
