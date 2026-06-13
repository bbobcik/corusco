# COR-062 Add tester command invocation

## Commit Message

```text
COR-062 Add tester command invocation
```

## Roadmap Slice

Roadmap Stage 19: Swing MVP Test Harness, action invocation by generated
`ActionKey`.

## Context

`COR-061` introduced the EDT-safe `SwingMvpTester` shell and generated
component lookup. Stage 19 next needs tester-level command invocation so tests
can exercise presenter actions through generated `ActionKey` constants without
reaching into button fields, Swing `Action` instances, reflection, or JavaBeans
property names.

## Scope

- Extend `SwingMvpTester` with an optional `CommandSet`.
- Add a factory overload that builds commands on the EDT after presenter
  construction.
- Add command lookup and invocation by `ActionKey`.
- Add basic command enabled/selected assertions.
- Add tests and an example with method-body comments.

## Out Of Scope

- Field input helpers.
- Table selection helpers.
- Problem, behavior, or table-state assertions.
- Generated-source compiler helpers.
- Automatic command discovery from Swing components or presenter fields.

## Implementation Steps

1. Add a command-set field and factory overload to `SwingMvpTester`.
2. Add `findCommand`, `requireCommand`, `executeCommand`,
   `assertCommandEnabled`, and `assertCommandSelected`.
3. Add tests for EDT command factories, invocation, disabled commands, missing
   commands, enabled/selected assertions, and duplicate command keys.
4. Update the tester example to use generated-style action keys.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- Commands are supplied explicitly through `CommandSet`.
- Command factories and command execution run on the EDT.
- Commands can be invoked by generated `ActionKey`.
- Missing and duplicate command keys fail clearly.
- No reflection, JavaBeans, or property-path APIs are introduced.
