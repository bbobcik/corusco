# COR-053 Add async field validation

## Commit Message

```text
COR-053 Add async field validation
```

## Roadmap Slice

Roadmap Stage 17: Async Tasks and Async Validation, async field validation API
slice.

## Context

`COR-050` through `COR-052` established task execution, EDT callback delivery,
and generation tokens for stale-result suppression. The next coherent step is a
Swing-free async field-validation controller that runs validator work through a
`TaskService`, exposes busy/problem values, and ignores results for old field
values.

## Scope

- Add `AsyncFieldValidator<O, T>`.
- Add `AsyncFieldValidation<O, T>`.
- Observe a `ReadableValue<T>` and submit async validation on value changes.
- Expose `ReadableValue<ProblemSet>` and `ReadableValue<Boolean>` state.
- Use `GenerationCounter` to ignore stale success/failure/cancel callbacks.
- Surface current validator failures as typed validation problems.
- Cancel outstanding tasks on close.
- Add tests and an example with method-body comments.

## Out Of Scope

- Swing bindings for async validation state.
- Debounce/timing policy.
- Async form/cross-field validation.
- Generated behavior-plan emission.
- Busy overlay behavior.

## Implementation Steps

1. Add the async validator interface.
2. Add async field validation controller.
3. Add tests for initial validation, value-change validation, stale results,
   failure mapping, busy state, and close cleanup.
4. Add a focused example.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- Validation work runs through `TaskService`.
- Problem and busy values are observable.
- Field changes schedule new validation.
- Older validation results cannot overwrite newer results.
- Validator failure creates a field-targeted validation problem.
- Close cancels outstanding work and stops value observation.
- No Swing, reflection, JavaBeans, or property-path APIs are introduced.
