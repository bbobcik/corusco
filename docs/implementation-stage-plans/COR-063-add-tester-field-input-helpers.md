# COR-063 Add tester field input helpers

## Commit Message

```text
COR-063 Add tester field input helpers
```

## Roadmap Slice

Roadmap Stage 19: Swing MVP Test Harness, field input helpers.

## Context

`COR-061` added component lookup by generated `ComponentKey`, and `COR-062`
added command invocation by generated `ActionKey`. Stage 19 next needs concise
field input operations so tests can exercise generated views in the style shown
by the roadmap without direct component mutation scattered through test bodies.

## Scope

- Add tester helpers for text entry into generated text components.
- Add tester helpers for generated checkbox/toggle selection.
- Add tester helpers for generated combo-box item selection.
- Keep lookup and mutation inside one EDT operation.
- Add tests and update the tester example with method-body comments.

## Out Of Scope

- Problem assertions.
- Table selection helpers.
- Behavior-installed or table-state assertions.
- Generated-source compiler helpers.
- Parsing/validation-specific model assertions.

## Implementation Steps

1. Add `enterText(ComponentKey, String)` for `JTextComponent` descendants.
2. Add `setSelected(ComponentKey, boolean)` for `AbstractButton` descendants.
3. Add `selectItem(ComponentKey, Object)` for `JComboBox` descendants.
4. Add tests for EDT execution, missing-component diagnostics, and type-safe
   component-key routing.
5. Update the tester example to demonstrate component-key input helpers.
6. Run test/build checks and commit the slice.

## Acceptance Checks

- Text, checkbox, and combo input helpers mutate components on the EDT.
- Helpers use generated component keys, not reflection or property paths.
- Missing component keys fail clearly.
- Existing component lookup and command helpers continue to pass.
- No reflection, JavaBeans, or property-path APIs are introduced.
