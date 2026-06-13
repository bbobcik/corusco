# COR-010 Add Command Action Core

Commit message:

```text
COR-010 Add command action core
```

## Roadmap Coverage

Roadmap Stage 7: Commands and Swing Actions.

## Objective

Introduce the type-safe command runtime model and Swing `Action` bridge used by
buttons, menu items, toolbar buttons, and key bindings.

## Dependencies

- Requires COR-002 lifecycle subscriptions.
- Requires COR-003 observable value primitives.
- Requires COR-004 typed action/resource keys.
- Requires COR-009 behavior scope.

## Scope

Add command APIs in `corusco-core`, under packages such as:

- `cz.auderis.corusco.core.command`

Candidate public API:

- `Command`
- `MutableCommand`
- `CommandFactory`
- `CommandSet`
- `ActionDescriptor`
- `AcceleratorDescriptor`
- `CommandHandler`

Add Swing command integration in `corusco-swing`, under packages such as:

- `cz.auderis.corusco.swing.command`

Candidate public API:

- `CommandResources`
- `SwingActionAdapter`

Add behavior helpers for:

- command button binding
- command menu item binding
- command key binding

## Required Deliverables

- New code with Javadoc: command identity, resource-key metadata, enabled and
  selected state ownership, Swing EDT expectations, and adapter disposal must
  be documented.
- Tests: cover command execution, enabled-state propagation, selected-state
  propagation, duplicate command-key rejection, one command backing multiple
  Swing controls, key binding dispatch, and behavior disposal.
- Examples: add a small command example that binds the same command to multiple
  Swing controls and includes body comments explaining ownership and generated
  metadata shape.

## Out of Scope

- Annotation-generated action descriptors.
- Real resource bundle lookup service.
- Icons and image loading.
- Async command/task wrapping.
- Dialog default-button policy.

## Implementation Steps

1. Define command descriptors and mutable command state in `corusco-core`.
2. Implement `CommandFactory` and `CommandSet`.
3. Implement a disposable Swing `Action` adapter that observes command state.
4. Add command binding behaviors using `BehaviorScope`.
5. Add examples and tests.
6. Run `./gradlew clean build`.

## Acceptance Checks

- One command can back a button and menu item through Swing actions.
- Updating command enabled state updates all bound Swing controls.
- Toggle command selected state works for toggle buttons and checkbox menu
  items.
- Key bindings dispatch the same command handler as buttons/menu items.
- Command metadata uses `ResourceKey<String>` for text and tooltip metadata.
- No Swing dependency is introduced into `corusco-core`.

## Review Focus

- The core model must stay independent from Swing/AWT classes.
- Swing listeners/subscriptions must be disposable and EDT-confined.
- Toggle state must have a single owner so buttons, menu items, and key bindings
  do not drift apart.
