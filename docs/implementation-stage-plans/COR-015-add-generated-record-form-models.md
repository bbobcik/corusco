# COR-015 Add Generated Record Form Models

Commit message:

```text
COR-015 Add generated record form models
```

## Roadmap Coverage

Roadmap Stage 9: Annotation Processor Phase 2, first generated form-model
slice.

## Objective

Generate concrete form model classes for annotated immutable records using the
runtime form, converter, descriptor, and validation primitives already present.

## Dependencies

- Requires COR-006 field model core.
- Requires COR-007 validation rule core.
- Requires COR-011 through COR-014 annotation processor metamodel output.

## Scope

For each `@CoruscoForm` record, generate:

- `<RecordName>FormModel extends AbstractFormModel<RecordName>`
- public final field members for generated field models
- descriptor list exposure
- generated validator installation for field constraints
- `createResult()` that calls the canonical record constructor directly

Supported field shapes:

- `@TextField String`
- `@TextField Integer`
- `@TextField BigDecimal`
- `@DateField LocalDate`
- `@CheckBox boolean` / `Boolean`
- `@ComboBox` declared value types

## Required Deliverables

- Generated form model code with readable, deterministic source.
- Processor tests proving generated source and behavior.
- Example update showing a generated form model in downstream compilation.
- Critical review against reflection policy and generated-code readability.

## Out of Scope

- Mutable bean support.
- Custom converter/resource service injection.
- Cross-field/form-level generated validators.
- Generated behavior plans or view contracts.
- Async validation.

## Implementation Steps

1. Extend processor field specs with model-construction metadata.
2. Generate `<RecordName>FormModel` beside existing metadata classes.
3. Generate rule-set construction from field constraint metadata.
4. Add JavaCompiler tests that instantiate the generated model.
5. Add example coverage in `corusco-examples`.
6. Run `./gradlew clean build`.

## Acceptance Checks

- Generated model compiles and is readable.
- `toResult()` calls the record constructor directly.
- Reset and dirty state work through `AbstractFormModel`.
- Generated validators run and block uncommittable results.
- Generated code uses no reflection.
