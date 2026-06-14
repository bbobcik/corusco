# Corusco Dialog Guide

Corusco dialog support is a controller layer for transactional form editing.
`FormDialog` owns OK, Apply, Cancel, validation-summary, keyboard,
active-editor commit, and lifecycle semantics. `FormDialogShell` can host that
controller in a minimal native `JDialog` when an application wants the standard
Swing shell without adopting a larger application framework.

Use a form dialog when edits must remain transactional until the user accepts
them:

```text
domain value
    -> edit record
        -> FormModel<R>
            -> FormDialog<P, R>
                -> OK / Apply / Cancel commands
                -> validation summary and focus
                -> keyboard bindings
                -> lifecycle owner
    -> DialogResult<R>
```

## Core Types

| Type | Role |
| --- | --- |
| `DialogResult<R>` | Swing-free sealed result: accepted value or cancellation. |
| `FormDialog<P, R>` | EDT-bound controller for OK, Apply, Cancel, active-editor commit, command state, and terminal result. |
| `DirtyState` | Explicit aggregate dirty-state hook used before cancellation. |
| `CancelConfirmation` | UI-policy hook for dirty-cancel confirmation. |
| `FormDialogShell` | Minimal modal `JDialog` host for an existing `FormDialog` root component. |
| `FormDialogKeyboardBinding` | Root-pane ESC and default-button binding. |
| `FormDialogValidationBinding` | Pull-based validation summary and focus-first-problem binding. |
| `ProblemFocusResolver` | Typed problem-to-component resolver. |
| `FormDialogLifecycle` | Dialog-scoped owner for bindings, disposables, task services, and detachables. |

All Swing dialog helpers are EDT-bound. Create, mutate, and close them on the
Swing EDT.

## Dialog Results

`DialogResult<R>` is deliberately Swing-free:

```java
DialogResult<CustomerEdit> result = dialog.result();

if (result.isAccepted()) {
    save(result.acceptedValue().orElseThrow());
}
```

Accepted results carry a committed value. Cancelled results carry no value, so
callers cannot accidentally continue with a partially edited object.

Before OK creates an accepted result, `FormDialog` commits active editors,
checks `isCommittable()`, calls `toResult()`, accepts current form values as the
new baseline, stores `DialogResult.accepted(...)`, closes the controller, and
disables dialog commands.

## OK, Apply, and Cancel

Create a controller around an existing form model and a root component:

```java
GeneratedCustomerEditFormModel form = new GeneratedCustomerEditFormModel(original);
JPanel root = new JPanel();
DirtyState dirtyState = () -> form.name.isDirty() || form.creditLimit.isDirty();
FormDialog<GeneratedCustomerEditFormModel, GeneratedCustomerEdit> dialog =
        new FormDialog<>(form, root, dirtyState, confirmDiscard);
```

`okCommand()` and `applyCommand()` are enabled only when the form is currently
committable. `cancelCommand()` remains enabled until the controller closes.
Call `refreshCommandState()` after form state changes that may affect
committability.

Apply follows the same commit path as OK but does not close the controller:

```java
dialog.applyCommand().execute();
dialog.lastAppliedResult().ifPresent(this::saveDraft);
```

Cancel checks dirty state before discarding edits. Clean forms cancel
immediately. Dirty forms call `CancelConfirmation.confirmCancel()`; returning
false leaves the controller open and preserves edits.

## Native Dialog Shell

Use `FormDialogShell` when the application wants a simple modal `JDialog` but
still owns the root layout, buttons, resource lookup, and dirty-cancel UI:

```java
FormDialogShell<GeneratedCustomerEditFormModel, GeneratedCustomerEdit> shell =
        FormDialogShell.create(ownerWindow, "Customer", dialog);

okButton.addActionListener(event -> shell.accept());
applyButton.addActionListener(event -> shell.apply());
cancelButton.addActionListener(event -> shell.cancel());

DialogResult<GeneratedCustomerEdit> result = shell.showModal();
```

The shell sets the controller root as the dialog content pane. Native window
closing delegates to `dialog.cancel()`, so `CancelConfirmation` behavior is the
same as a Cancel button. `shell.close()` is lifecycle cleanup and delegates to
`FormDialog.close()`, bypassing dirty confirmation in the same way the
controller does.

## Active Editor Commit

OK and Apply call `SwingEditors.commitActiveEditor(root)` before asking the form
for a result. This matters for Swing controls that hold user edits outside the
form model until editing stops, such as tables, formatted text fields, and
spinners.

```java
table.editCellAt(0, 0);
((JTextField) table.getEditorComponent()).setText("Alicia");

dialog.okCommand().execute();
```

If an active editor rejects the commit, OK/Apply return false and the dialog
stays open. This keeps result creation behind a single commit gate instead of
duplicating editor-specific code in OK buttons.

## Keyboard Binding

Install keyboard behavior on the root pane owned by the native modal shell:

```java
try (FormDialogKeyboardBinding keyboard =
             FormDialogKeyboardBinding.install(rootPane, dialog, okButton)) {
    showModalShell();
}
```

The binding maps ESC to the existing cancel command and optionally sets the
default button. Closing restores the previous ESC mapping, previous action, and
previous default button, which is important for tests and reusable shells.

## Validation Summary and Focus

`FormDialogValidationBinding` is pull-based. The current form model contract
exposes synchronous problems rather than an observable problem stream, so
presenters refresh the summary after relevant form changes:

```java
ProblemFocusResolver focusResolver = ProblemFocusResolver.componentTargets(Map.of(
        GeneratedCustomerEditView.NAME_COMPONENT, view.nameField()
));

try (FormDialogValidationBinding validation =
             FormDialogValidationBinding.install(dialog, summaryLabel, focusResolver)) {
    validation.refresh();
    validation.focusFirstProblem();
}
```

The summary text is empty when there are no problems, the first message when
there is one problem, and a count plus first message when there are multiple
problems. `focusFirstProblem()` walks problems by severity and asks the resolver
for a target component.

Keep focus mapping explicit through typed component keys or direct lambdas. Do
not introduce JavaBeans property paths or reflective component lookup.

## Lifecycle Ownership

Use `FormDialogLifecycle` as the owner for resources created for one modal
dialog instance:

```java
FormDialogLifecycle lifecycle = FormDialogLifecycle.of(dialog);
lifecycle.addBinding(validationBinding);
lifecycle.addDisposable(taskService);
lifecycle.addDetachable(detailLoader);
```

Closing the lifecycle releases registered resources and then closes the dialog
controller. Use one lifecycle per open dialog instance so repeated open/close
cycles do not accumulate listeners, task callbacks, or detachable backing
state.

## Binding Commands to Buttons

Dialog commands are ordinary `MutableCommand` instances. Bind them through the
command behavior layer:

```java
try (BehaviorScope behaviors = new BehaviorScope()) {
    behaviors.install(okButton, List.of(CommandBehaviors.commandButton(dialog.okCommand(), resources)));
    behaviors.install(applyButton, List.of(CommandBehaviors.commandButton(dialog.applyCommand(), resources)));
    behaviors.install(cancelButton, List.of(CommandBehaviors.commandButton(dialog.cancelCommand(), resources)));
}
```

The built-in dialog command resource keys are:

- `FormDialog.OK_TEXT`
- `FormDialog.APPLY_TEXT`
- `FormDialog.CANCEL_TEXT`

Applications may resolve those keys through the same `CommandResources`
mechanism used for presenter commands.

## Testing Dialogs

Prefer headless controller tests and EDT-aware Swing tests:

- `DialogResult` accepted/cancelled behavior;
- OK creates an accepted result and closes the controller;
- Apply creates `lastAppliedResult()` without closing;
- Cancel resets the form and produces a cancelled result;
- dirty-cancel confirmation blocks or allows cancellation;
- OK/Apply commit active editors before reading the form result;
- ESC delegates through the cancel command;
- keyboard bindings restore previous root-pane state;
- validation summary text and focus resolution;
- lifecycle cleanup on repeated open/close cycles.

See `FormDialogExample`, `DirtyCancelDialogExample`,
`DialogValidationExample`, `DialogKeyboardExample`, `DialogLifecycleExample`,
and `DialogActiveEditorExample` for compiling scenarios with method-body
comments.

See [Testing Guide](testing.md) for the shared Swing MVP and generated-source
testing patterns used across examples.

## Current Limits

- Dialog validation summary is refreshed explicitly; there is no observable
  problem-stream binding yet.
- Dirty state is supplied by an explicit hook. Generated code should compose it
  from typed fields instead of reflection.
- Dirty-cancel UI is application-owned through `CancelConfirmation`.
- Generated form behavior plans do not yet generate a complete dialog shell.
