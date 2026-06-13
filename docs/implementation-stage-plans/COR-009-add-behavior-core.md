# COR-009 Add Behavior Core

Commit message:

```text
COR-009 Add behavior core
```

## Roadmap Coverage

Roadmap Stage 6: Behavior Core.

## Objective

Introduce the Wicket-inspired behavior abstraction used to install reusable,
ordered, disposable component extensions on Swing components.

## Dependencies

- Requires COR-002 lifecycle subscriptions.
- Requires COR-004 typed keys.
- Requires COR-005 problem model.
- Requires COR-006 field model core.
- Requires COR-008 basic Swing bindings.

## Scope

Add behavior APIs in `corusco-swing`, under packages such as:

- `cz.auderis.corusco.swing.behavior`

Candidate public API:

- `ViewBehavior<C extends JComponent>`
- `BindingBehavior<C extends JComponent>`
- `DecorationBehavior<C extends JComponent>`
- `BehaviorScope`
- `BehaviorContext`
- `BehaviorFactory`
- `BehaviorPhase`
- `BehaviorKey`
- `BehaviorDescriptor`
- conflict/cardinality metadata

Initial standard behaviors:

- text field binding behavior
- checkbox binding behavior
- validation border behavior
- validation tooltip behavior
- select-all-on-focus behavior
- commit-on-enter behavior

## Required Deliverables

- New code with Javadoc: behavior APIs must document installation ownership,
  phase ordering, conflict handling, cardinality, and disposal.
- Tests: cover deterministic install/uninstall, reverse close order, phase
  ordering, duplicate/conflict failure, behavior-based text/checkbox bindings,
  validation decoration behavior, and standard focus/key behaviors.
- Examples: add a small behavior example that installs multiple behaviors on
  Swing components on the EDT without showing a native window.

## Out of Scope

- Command/action model.
- Help service and resource lookup.
- Dirty marker visuals beyond placeholders.
- Annotation-generated behavior plans.

## Implementation Steps

1. Define behavior identity, phases, cardinality, descriptor, context, and
   behavior interfaces.
2. Implement `BehaviorScope` with phase ordering and conflict checks.
3. Add standard behaviors built on COR-008 bindings and direct Swing listeners.
4. Add behavior example under `corusco-examples`.
5. Add focused Swing/EDT tests.
6. Run `./gradlew clean build`.

## Acceptance Checks

- Behaviors install and uninstall deterministically.
- Behavior phases/order are respected.
- Conflicting behaviors fail fast.
- `BehaviorScope` closes installed behaviors in reverse order.
- Behavior-based bindings pass the same core tests as direct bindings.
- Public behavior APIs have Javadoc.
- No behavior API is introduced into `corusco-core`.

## Review Focus

- Behaviors should wrap existing explicit bindings rather than duplicating
  binding logic.
- Conflict detection should be simple but strong enough to prevent double
  primary bindings.
- EDT requirements should remain explicit.
