# COR-054 Add Swing busy overlay

## Commit Message

```text
COR-054 Add Swing busy overlay
```

## Roadmap Slice

Roadmap Stage 17: Async Tasks and Async Validation, Swing `JLayer` busy overlay
behavior slice.

## Context

`COR-050` through `COR-053` added task execution, EDT callback delivery,
generation tracking, and async field validation state. Stage 17 still needs a
Swing overlay that blocks user input while a task or async validator is busy.
`JLayer` is the right Swing primitive because it can paint over a view and
intercept input without modifying every child component.

## Scope

- Add a reusable `LayerUI` that paints a semi-transparent busy overlay and
  consumes mouse/key/focus input while busy.
- Add a binding that observes `ReadableValue<Boolean>` busy state and updates a
  `JLayer` on the EDT.
- Add a standard decoration behavior for `JLayer`-hosted views.
- Restore the original `LayerUI` and subscription state on close.
- Add tests and an example with method-body comments.

## Out Of Scope

- Automatic parent replacement/wrapping of arbitrary components.
- Progress text, spinners, or animation.
- Generated behavior-plan emission.
- Dialog lifecycle integration.
- Async validation debounce/timing policy.

## Implementation Steps

1. Add busy overlay `LayerUI` and binding classes in the Swing task package.
2. Add a `StandardBehaviorKeys.BUSY_OVERLAY` key and a
   `StandardBehaviors.busyOverlay(...)` factory for `JLayer`.
3. Add tests for busy-state painting/input blocking, EDT installation,
   observable updates, and close cleanup.
4. Add a focused example that wraps a panel in a `JLayer`.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- A busy `JLayer` consumes input events before they reach the wrapped view.
- Non-busy state lets input pass through normally.
- Busy state changes repaint the layer and are driven by a
  `ReadableValue<Boolean>`.
- Closing the binding stops observation and restores any previous `LayerUI`.
- Behavior installation follows existing `BehaviorScope` phase/cardinality
  rules.
- No reflection, JavaBeans, or property-path APIs are introduced.
