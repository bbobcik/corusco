# COR-061 Add Swing MVP tester core

## Commit Message

```text
COR-061 Add Swing MVP tester core
```

## Roadmap Slice

Roadmap Stage 19: Swing MVP Test Harness, foundational tester and generated
component lookup slice.

## Context

Stage 18 completed modal dialog foundations. The next roadmap stage introduces
WicketTester-inspired support for headless Swing presenter, behavior, and
generated-view tests. The first coherent step should establish the tester's EDT
execution model and a typed way to locate generated components without
reflection or JavaBeans property paths.

## Scope

- Add a `cz.auderis.corusco.swing.testing` package.
- Add `SwingComponentKeys` as the Swing-side client-property convention for
  generated `ComponentKey` constants.
- Add `SwingMvpTester<V, P>` that creates views/presenters on the EDT and
  offers EDT-safe run/query helpers.
- Add recursive component lookup by typed `ComponentKey`.
- Add tests and an example with method-body comments.

## Out Of Scope

- Action invocation by `ActionKey`.
- Field input helpers.
- Table selection helpers.
- Problem, behavior, or table-state assertions.
- Generated-source compiler helpers.

## Implementation Steps

1. Add component marking helpers that store typed keys on Swing components.
2. Add the tester shell with EDT-owned view/presenter construction and
   run/query helpers.
3. Add typed recursive component lookup with duplicate and missing-component
   diagnostics.
4. Add tests for EDT execution, presenter creation, component marking, name
   fallback, missing components, duplicate keys, and type-safe lookup.
5. Add a focused example showing generated-style component constants and body
   comments explaining the testing convention.
6. Run test/build checks and commit the slice.

## Acceptance Checks

- View and presenter factories run on the EDT.
- Test interactions and queries can be executed on the EDT from normal JUnit
  threads.
- Components can be found by generated `ComponentKey` constants.
- Duplicate component keys fail clearly.
- No reflection, JavaBeans, or property-path APIs are introduced.
