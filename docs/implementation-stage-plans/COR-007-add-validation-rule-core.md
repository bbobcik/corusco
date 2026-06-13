# COR-007 Add Validation Rule Core

Commit message:

```text
COR-007 Add validation rule core
```

## Roadmap Coverage

Roadmap Stage 4: non-Swing validation pipeline and problem aggregation.

## Objective

Add a small validation layer that composes with the existing field, form, key,
and problem primitives without introducing Swing, reflection, or annotation
processor dependencies.

## Dependencies

- Requires COR-003 observable values.
- Requires COR-004 typed keys.
- Requires COR-005 problem model.
- Requires COR-006 field model core.

## Scope

Add validation APIs in `corusco-core`, under packages such as:

- `cz.auderis.corusco.core.validation`

Candidate public API:

- `FieldValidator<O, T>`
- `FormValidator<M>`
- `RuleSet<M>`
- `ValidationTiming`
- generated-compatible constraint helpers:
  - required
  - length
  - decimal min/max
  - integer min/max
  - regex
  - date past/future/present variants

Expected behavior:

- Field validation runs only after parse success.
- Parse problems and validation problems remain distinguishable by
  `ProblemSource`.
- Cross-field validation can declare typed field-key dependencies.
- Rule sets expose dependency metadata so later generated code and Swing
  bindings can revalidate only relevant rules.
- Form problem aggregation includes parse and validation problems.

## Required Deliverables

- New code with Javadoc: validation APIs and constraint helpers must document
  typed dependencies, timing, immutable problem output, and interaction with
  parse problems.
- Tests: cover required, length, numeric/date/range/regex validators,
  parse-failure skip behavior, cross-field dependencies, targeted
  revalidation, and form aggregation.
- Examples: add a simple validated field example and a cross-field handwritten
  form example. Include method-body comments where they clarify validation
  ownership or dependency metadata.

## Out of Scope

- Swing rendering of validation feedback.
- Async validation.
- Annotation-generated validators.
- Debounced validation execution.
- Resource localization of validation messages.

## Implementation Steps

1. Define validation timing and validator interfaces.
2. Add immutable validation rule entries with typed dependency metadata.
3. Implement `RuleSet<M>` with full and targeted validation.
4. Add generated-compatible constraint factory helpers.
5. Extend form models with validation problem aggregation without merging parse
   and validation sources.
6. Add field and cross-field validation examples.
7. Add focused tests for validators, dependency metadata, and aggregation.
8. Run `./gradlew clean build`.

## Acceptance Checks

- Field validation is skipped when a field has parse errors.
- Field validation runs after parse success.
- Validation problems target typed field keys or form targets.
- Cross-field rules declare typed dependencies and can be filtered by changed
  field key.
- Problem summaries can filter parse and validation problems separately.
- Public validation APIs have Javadoc.
- Examples demonstrate field and cross-field validation.
- No Swing, reflection, or annotation-processor dependency is introduced into
  `corusco-core`.

## Review Focus

- The validation layer should be useful by hand and straightforward for
  generated code to emit.
- The API should not turn constraint messages into localization policy.
- Dependency metadata should be explicit but not over-engineered.
