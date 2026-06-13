# COR-067 Add tester table-state assertions

## Commit Message

```text
COR-067 Add tester table-state assertions
```

## Roadmap Slice

Roadmap Stage 19: Swing MVP Test Harness, table-state assertion helpers.

## Context

`COR-061` through `COR-066` added generated-key interaction helpers, problem
assertions, and behavior-installed assertions. Stage 19 also needs table-state
assertions so presenter and generated binding tests can verify persisted table
layout state without duplicating `TableState` filtering and diagnostic code in
each test.

## Scope

- Add EDT-safe `TableState` assertions to `SwingMvpTester`.
- Cover table id, column visibility, column order, column width, and sort state.
- Add tests for matching state, EDT execution, missing columns/sorts, null
  inputs, and readable failures.
- Add a focused example with method-body comments.

## Out Of Scope

- Generated-source compiler helpers.
- New table-state persistence or controller behavior.
- Direct JTable column-model assertions beyond public `TableState`.

## Implementation Steps

1. Add generic `TableState` source assertion helpers to `SwingMvpTester`.
2. Cover nominal and failing assertions in tester tests.
3. Add an example that captures state from a `TableStateController` and asserts
   it through the tester.
4. Run test/build checks and commit the slice.

## Acceptance Checks

- Table-state assertions read state on the EDT.
- Assertion failures include table ids, column ids, or sort ids as applicable.
- Tests can assert state captured from a real `TableStateController`.
- No reflection, JavaBeans, or property-path APIs are introduced.
