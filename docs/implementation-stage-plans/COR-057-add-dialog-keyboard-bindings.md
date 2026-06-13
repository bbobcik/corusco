# COR-057 Add dialog keyboard bindings

## Commit Message

```text
COR-057 Add dialog keyboard bindings
```

## Roadmap Slice

Roadmap Stage 18: Modal Dialog Framework, ESC and default-button handling
slice.

## Context

`COR-055` and `COR-056` established dialog result flow, OK/Apply/Cancel
commands, and dirty-cancel confirmation. Stage 18 next needs keyboard behavior:
ESC should follow cancel logic, and dialogs should install a predictable default
button without forcing native dialog construction.

## Scope

- Add `FormDialogKeyboardBinding` in the Swing dialog package.
- Bind ESC on a `JRootPane` to `FormDialog.cancelCommand()`.
- Optionally install an OK/default button on the same root pane.
- Restore previous ESC mapping, action, and default button on close.
- Add tests and an example with method-body comments.

## Out Of Scope

- Native `JDialog` factory/display helpers.
- Validation summary and focus-first-problem behavior.
- Dirty-cancel confirmation UI.
- Presenter/task lifecycle integration.

## Implementation Steps

1. Add the keyboard binding class with EDT-bound install/close behavior.
2. Add tests for ESC cancellation, dirty-cancel veto, default button
   installation, restoration, and idempotent cleanup.
3. Add a focused example showing ESC and default-button wiring.
4. Run test/build checks and commit the slice.

## Acceptance Checks

- ESC invokes the same cancel command as a Cancel button.
- Dirty-cancel veto keeps the dialog open when ESC is pressed.
- Installing a default button updates the root pane default button.
- Closing restores any previous ESC input/action mapping and default button.
- No reflection, JavaBeans, or property-path APIs are introduced.
