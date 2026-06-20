# Corusco Dialog Guide

Corusco dialog support is a controller layer for transactional form editing.
`FormDialog` owns OK, Apply, Revert, Cancel, validation-summary, keyboard,
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
                -> OK / Apply / Revert / Cancel commands
                -> validation summary and focus
                -> keyboard bindings
                -> lifecycle owner
    -> DialogResult<R>
```

## Core Types

| Type | Role |
| --- | --- |
| `DialogResult<R>` | Swing-free sealed result: accepted value, cancellation, or explicit revert. |
| `FormDialog<P, R>` | EDT-bound controller for OK, Apply, Revert, Cancel, active-editor commit, command state, and terminal result. |
| `DirtyState` | Explicit aggregate dirty-state hook used before cancellation. |
| `DirtyStates` | Small composition helpers for aggregate dirty-state policies. |
| `CancelConfirmation` | UI-policy hook for dirty-cancel confirmation. |
| `RevertPolicy` | Optional pre-dialog restoration policy for dialogs that can undo applied and unapplied changes. |
| `FormDialogActionState` | Presentation models for Apply and Revert action enablement. |
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

Accepted results carry a committed value. Cancelled and reverted results carry
no accepted value, so callers cannot accidentally continue with a partially
edited object. Reverted results are distinct because the controller has run an
explicit application-provided restoration policy.

Before OK creates an accepted result, `FormDialog` commits active editors,
checks `isCommittable()`, calls `toResult()`, accepts current form values as the
new baseline, stores `DialogResult.accepted(...)`, closes the controller, and
disables dialog commands.

## OK, Apply, Revert, and Cancel

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

After a successful Apply, the dialog accepts current form values as the new
baseline and remembers the applied result. If the user later presses Cancel,
the terminal result is accepted with that last applied value. Cancel discards
only edits made after the last successful Apply. A failed Apply does not update
`lastAppliedResult()`.

Cancel checks dirty state before discarding edits. Clean forms cancel
immediately. Dirty forms call `CancelConfirmation.confirmCancel()`; returning
false leaves the controller open and preserves edits.

Revert is optional and separate from `FormModel.reset()`. Reset returns to the
current baseline, and Apply intentionally moves that baseline. Revert should be
configured only when the application can restore the pre-dialog state, including
changes that were already applied:

```java
FormDialog<CustomerSettingsSession, CustomerSettings> dialog =
        new FormDialog<>(session, root, currentBaselineDirty, confirmDiscard, revertPolicy);

if (dialog.revert()) {
    assert dialog.result().isReverted();
}
```

Use `FormDialogActionState` when buttons need presentation models. Apply
typically follows current-baseline dirty state; Revert follows pre-dialog dirty
state:

```java
FormDialogActionState actions =
        new FormDialogActionState(dialog, currentBaselineDirty, preDialogDirty);

boolean applyEnabled = actions.applyAction().enabled().value();
boolean revertEnabled = actions.revertAction().enabled().value();
```

## Native Dialog Shell

Use `FormDialogShell` when the application wants a simple modal `JDialog` but
still owns the root layout, buttons, resource lookup, and dirty-cancel UI:

```java
FormDialogShell<GeneratedCustomerEditFormModel, GeneratedCustomerEdit> shell =
        FormDialogShell.create(ownerWindow, "Customer", dialog);

okButton.addActionListener(event -> shell.accept());
applyButton.addActionListener(event -> shell.apply());
revertButton.addActionListener(event -> shell.revert());
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

For multi-form dialogs, compose resolvers in child order and reveal the owning
view before focus is requested:

```java
ProblemFocusResolver resolver = ProblemFocusResolver.firstOf(
        ProblemFocusResolver.withPreparation(
                () -> tabs.setSelectedComponent(identityPanel),
                ProblemFocusResolver.fieldTargets(Map.of(
                        CustomerIdentityFields.NAME.asFieldKey(),
                        identityView.nameField()
                ))
        ),
        ProblemFocusResolver.withPreparation(
                () -> tabs.setSelectedComponent(securityPanel),
                ProblemFocusResolver.fieldTargets(Map.of(
                        SecuritySettingsFields.PASSWORD.asFieldKey(),
                        securityView.passwordField()
                ))
        )
);
```

`FormDialogValidationBinding` still reads one `FormModel`. A composite parent
therefore lets child and parent validation problems appear in one summary while
the resolver keeps navigation explicit.

## Multi-form Dialog Sessions

`FormDialog` accepts any `FormModel<R>`, so a composite parent model does not
need a separate dialog controller. Build a handwritten parent session with
`AbstractCompositeFormModel<R>`, pass it to `FormDialog`, and compose dirty
state explicitly:

```java
CustomerDialogSession session = new CustomerDialogSession(identityForm, securityForm);
DirtyState dirty = DirtyStates.any(
        () -> identityForm.name.isDirty(),
        () -> securityForm.password.isDirty()
);

FormDialog<CustomerDialogSession, CustomerDialogResult> dialog =
        new FormDialog<>(session, root, dirty, confirmDiscard);
```

Generated child presentation models remain useful. A parent presenter can group
them beside dialog action state without committing presentation state into the
result:

```java
final class CustomerDialogPresentation {
    final CustomerIdentityPresentationModel identity;
    final SecuritySettingsPresentationModel security;
    final FormDialogActionState actions;
}
```

This keeps the semantic aggregate independent of Swing companion generation.
Child packages may opt into generated Swing `*View`, `*BehaviorPlan`, and
`*Bindings`, but the core session only needs generated form and presentation
models.

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
- `FormDialog.REVERT_TEXT`
- `FormDialog.CANCEL_TEXT`

Applications may resolve those keys through the same `CommandResources`
mechanism used for presenter commands.

## Testing Dialogs

Prefer headless controller tests and EDT-aware Swing tests:

- `DialogResult` accepted, cancelled, and reverted behavior;
- `DialogResult` reverted behavior when a Revert policy succeeds;
- OK creates an accepted result and closes the controller;
- Apply creates `lastAppliedResult()` without closing;
- Apply followed by Cancel returns the last applied value as accepted;
- Cancel before Apply resets the form and produces a cancelled result;
- dirty-cancel confirmation blocks or allows cancellation;
- Revert restores pre-dialog state only when a policy is configured;
- Apply/Revert component state follows current-baseline and pre-dialog dirty state;
- OK/Apply commit active editors before reading the form result;
- ESC delegates through the cancel command;
- keyboard bindings restore previous root-pane state;
- validation summary text and focus resolution;
- lifecycle cleanup on repeated open/close cycles.

See `FormDialogExample`, `ApplyRevertDialogExample`,
`DirtyCancelDialogExample`, `DialogValidationExample`, `DialogKeyboardExample`,
`DialogLifecycleExample`, `DialogActiveEditorExample`, and
`MultiFormDialogSessionExample` for compiling scenarios with method-body
comments.

See [Testing Guide](testing.md) for the shared Swing MVP and generated-source
testing patterns used across examples.

## Current Limits

- Dialog validation summary is refreshed explicitly; there is no observable
  problem-stream binding yet.
- Dirty state is supplied by an explicit hook. Generated code should compose it
  from typed fields instead of reflection.
- Revert is optional. Applications must supply a policy that can really restore
  or compensate applied and unapplied changes.
- Dirty-cancel UI is application-owned through `CancelConfirmation`.
- Generated form behavior plans do not yet generate a complete dialog shell.
