# COR-006 Add Field Model Core

Commit message:

```text
COR-006 Add field model core
```

## Roadmap Coverage

Roadmap Stage 3: non-Swing portions of Field Models, Text Models, Conversion,
and Form Model Core.

## Objective

Implement handwritten field and text-field model primitives so form behavior can
be proven without Swing or generated code.

## Dependencies

- Requires COR-003 observable value primitives.
- Requires COR-004 typed keys.
- Requires COR-005 problem model.

## Scope

Add form and conversion APIs in `corusco-core`, under packages such as:

- `cz.auderis.corusco.core.form`
- `cz.auderis.corusco.core.convert`

Candidate public API:

- `FieldModel<T>`
- `TextFieldModel<T>`
- `ParseState<T>`
- `StringConverter<T>`
- `ParseResult<T>`
- `FormModel<R>`
- `AbstractFormModel<R>`

Initial converter coverage:

- `String`
- `Integer`
- `Long`
- `BigDecimal`
- `LocalDate`
- `Enum`

Expected model behavior:

- A text field can hold raw text that is invalid or incomplete.
- The previous semantic value is not destroyed by invalid raw text.
- Parse failures appear as typed problems.
- Dirty state compares current semantic value to original value.
- Touched state records user interaction.
- Reset restores original value and raw text.
- Accepting current values updates the original baseline.
- `toResult()` is blocked for uncommittable forms.

## Required Deliverables

- New code with Javadoc: public form, field, parse, converter, and result APIs
  must document editing semantics, invalid intermediate input, null/empty
  policy, dirty/touched state, problem propagation, and `toResult()` behavior.
- Tests: cover converters, parse success and failure, invalid raw text, dirty
  and touched state, reset, accepting current values, problem aggregation, and
  blocked result creation.
- Examples: add a simple single-field example and a more advanced handwritten
  record-backed form example. Revisit earlier value/key/problem examples so they
  demonstrate the current preferred form-model usage where relevant.

## Out of Scope

- Swing `Document` or component binding.
- Validation rules beyond parse problems.
- Behavior installation.
- Annotation-generated form models.
- Async validation.

## Implementation Steps

1. Define parse result and parse state types.
2. Define `StringConverter<T>` and converter options for empty/null policy.
3. Implement standard converters with focused edge-case tests.
4. Implement `FieldModel<T>` over observable values and field keys.
5. Implement `TextFieldModel<T>` with raw text, parse state, semantic value,
   dirty state, touched state, and parse problems.
6. Implement `FormModel<R>` and `AbstractFormModel<R>` with reset,
   committability, problem aggregation, and `toResult()` guard hooks.
7. Add simple and advanced form-model examples under `corusco-examples`.
8. Add one handwritten test form model to prove composition.
9. Run `./gradlew clean build`.

## Acceptance Checks

- Invalid raw text can be stored without replacing the previous semantic value.
- Parse success updates semantic value and clears parse problems for that field.
- Parse failure creates a typed problem target for the field.
- Empty/null handling is configurable and tested.
- Dirty state changes when current semantic value differs from the original.
- Touched state can be observed and reset according to documented semantics.
- Reset restores original values.
- Accepting current values updates dirty-state baselines.
- `toResult()` refuses invalid or uncommittable forms.
- All behavior is testable without Swing.
- Public form/conversion APIs have Javadoc.
- Examples demonstrate simple and record-backed handwritten form usage.

## Review Focus

- Text editing semantics honor the roadmap's "invalid intermediate input is
  valid UI state" principle.
- Form model API is generated-code friendly but still useful by hand.
- No Swing, annotation processor, or reflection dependency is introduced.
