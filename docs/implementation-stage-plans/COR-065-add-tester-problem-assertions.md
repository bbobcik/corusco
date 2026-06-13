# COR-065 Add tester problem assertions

## Commit Message

```text
COR-065 Add tester problem assertions
```

## Roadmap Slice

Roadmap Stage 19: Swing MVP Test Harness, problem assertion helpers.

## Context

`COR-061` through `COR-064` added the tester shell, generated component lookup,
command invocation, field input, and table selection helpers. The next missing
tester capability is direct problem assertion support so presenter tests can
verify form, field, table, or presenter-owned `ProblemSet` instances without
open-coded EDT queries and ad hoc filtering.

## Scope

- Add EDT-safe assertions for matching and absent problems.
- Add count assertions for filtered problem sets.
- Add typed field-key/problem-code convenience assertions.
- Add tests for nominal behavior, absence checks, EDT execution, null handling,
  and readable failure messages.
- Add a focused example with method-body comments.

## Out Of Scope

- Behavior-installed assertions.
- Table-state assertions.
- Generated-source compiler helpers.
- New problem model features.

## Implementation Steps

1. Add generic `ProblemSet` assertion helpers to `SwingMvpTester`.
2. Add typed field-key/problem-code convenience overloads.
3. Cover the helper behavior in `SwingMvpTesterTest`.
4. Add an example that drives Swing input and asserts resulting model problems.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- Problem assertions read problem state on the EDT.
- Tests can assert field-targeted generated-style problem codes.
- Failure messages include the expected target/code or matching count.
- No reflection, JavaBeans, or property-path APIs are introduced.
