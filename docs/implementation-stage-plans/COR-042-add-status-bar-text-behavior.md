# COR-042 Add status-bar text behavior

## Commit Message

```text
COR-042 Add status-bar text behavior
```

## Roadmap Slice

Roadmap Stage 15: Help, Tooltips, Resources, and Accessibility,
status-bar text behavior slice.

## Context

COR-037 through COR-041 established typed resources, help dispatch, F1
behavior, core tooltip composition, and Swing tooltip behavior. The next
descriptor-driven UI affordance is status-bar text: focused components should
be able to publish concise guidance to a shared status label without leaking
listeners or permanently overwriting status text owned by the surrounding
screen.

## Scope

- Add Swing binding support for focus-scoped status text.
- Add static and observable status text overloads.
- Add `StandardBehaviors.statusText(...)` behavior helpers.
- Restore the previous status-bar label text when focus leaves or the binding
  is closed.
- Add tests for focus transitions, dynamic text changes, cleanup, and behavior
  installation.
- Add an example that resolves stable resource text and explains ownership with
  method-body comments.

## Out Of Scope

- Accessible name/description behavior.
- Table header tooltip behavior.
- Table cell tooltip behavior.
- Generated status resource descriptors.

## Implementation Steps

1. Add a status-text behavior key.
2. Add `BindingFactory.statusText(...)` overloads for static and observable
   status text.
3. Add `StandardBehaviors.statusText(...)` factory methods.
4. Add focused unit tests and a small example.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- Focus gain sets the shared status label to the configured text.
- Focus loss restores the status label text that was active before focus gain.
- Observable status text updates the label while the component is focused.
- Closing the binding removes listeners and restores the previous status text
  if the component still owns it.
- The example documents how stable resource ids feed status text without using
  reflection, JavaBeans, or property paths.
