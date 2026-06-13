# COR-005 Add Problem Model

Commit message:

```text
COR-005 Add problem model
```

## Roadmap Coverage

Roadmap Stage 2: problem, target, source, set, and filter portion of Typed Keys
and Problem Model.

## Objective

Add the typed problem model used by parsing, validation, form committability,
feedback decorators, summaries, and generated validation code.

## Dependencies

- Requires COR-004 typed keys.

## Scope

Add problem APIs in `corusco-core`, under a package such as
`cz.auderis.corusco.core.problem`.

Candidate public API:

- `ProblemCode`
- `ProblemSeverity`
- `ProblemTarget`
- `Problem`
- `ProblemSet`
- `ProblemSource`
- `ProblemFilter`

Expected target hierarchy:

- Form target.
- Field target based on `FieldKey<O, T>`.
- Row target.
- Cell target.
- Component target based on `ComponentKey<C>`.

Expected filtering:

- By severity.
- By target kind.
- By exact field key.
- By row or cell identity.
- By source where useful.

## Required Deliverables

- New code with Javadoc: problem codes, severities, targets, sets, sources, and
  filters must document typing, ordering, immutability, and filtering semantics.
- Tests: cover target creation, severity ordering, filtering by each supported
  dimension, set aggregation behavior, equality, and no-string field targeting.
- Examples: add a simple field-problem example and a more advanced filtering
  example. Revisit typed-key examples to show how field keys target problems.

## Out of Scope

- Validation rule execution.
- Swing rendering of problems.
- Tooltip or border decorators.
- Localization/resource lookup for problem messages.
- Generated validators.

## Implementation Steps

1. Define `ProblemSeverity` ordering.
2. Define `ProblemCode` as a typed stable identity, not an enum-only design.
3. Define `ProblemTarget` as a sealed hierarchy if that remains practical on
   JDK 25.
4. Define immutable `Problem` records/classes.
5. Implement `ProblemSet` as an immutable or controlled-mutation aggregate.
6. Implement composable `ProblemFilter` predicates.
7. Add problem-targeting and filtering examples under `corusco-examples`.
8. Add tests for filtering, equality, severity ordering, and typed field
   targeting.
9. Run `./gradlew clean build`.

## Acceptance Checks

- Problems can target a form, field, row, cell, or component.
- Field problem targets preserve owner and value typing through `FieldKey`.
- Filtering by field, form, severity, row, and cell is covered by tests.
- Pattern matching over problem targets is clean and exhaustive where possible.
- No public problem-targeting API uses arbitrary field-name strings.
- Public problem APIs have Javadoc.
- Examples demonstrate field targeting and problem filtering.
- No Swing dependency is introduced into `corusco-core`.

## Review Focus

- The target model is strong enough for validation and table stages.
- `ProblemSet` behavior is deterministic and easy to reason about.
- The API leaves localization to later resource stages.
