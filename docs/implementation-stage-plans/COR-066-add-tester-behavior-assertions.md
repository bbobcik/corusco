# COR-066 Add tester behavior assertions

## Commit Message

```text
COR-066 Add tester behavior assertions
```

## Roadmap Slice

Roadmap Stage 19: Swing MVP Test Harness, behavior-installed assertion helpers.

## Context

`COR-061` through `COR-065` added generated-key Swing interaction helpers and
problem assertions. Stage 19 also requires assertions that generated behavior
plans installed the expected behaviors. `BehaviorScope` already owns behavior
installation, conflict checks, and disposal, but it does not expose installed
behavior keys for tests to inspect.

## Scope

- Track installed `BehaviorKey` values per component in `BehaviorScope`.
- Expose EDT-only installed behavior key queries without leaking behavior
  instances or mutable internals.
- Add `SwingMvpTester` assertions for installed and absent behaviors by
  generated `ComponentKey`.
- Add tests for ordering/tracking, close cleanup, EDT execution, null handling,
  and readable assertion failures.
- Add a focused example with method-body comments.
- Fix the existing generated-view-plan example so it does not directly
  reference a behavior-plan class generated from the same source set.

## Out Of Scope

- Table-state assertions.
- Generated-source compiler helpers.
- Behavior factory generation.
- Runtime inspection of private behavior instances.

## Implementation Steps

1. Add installed-key tracking and query methods to `BehaviorScope`.
2. Add behavior assertion helpers to `SwingMvpTester`.
3. Cover behavior tracking and tester assertions.
4. Add an example that installs a behavior plan and asserts it through the
   tester.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- Behavior assertions execute on the EDT.
- Tests can assert installed standard behavior keys by generated component key.
- Behavior tracking is cleared when the scope is closed.
- Failure messages include the component key and behavior key.
- No reflection, JavaBeans, or property-path APIs are introduced.
