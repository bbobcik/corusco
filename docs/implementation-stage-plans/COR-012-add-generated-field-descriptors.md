# COR-012 Add Generated Field Descriptors

Commit message:

```text
COR-012 Add generated field descriptors
```

## Roadmap Coverage

Roadmap Stage 8: Annotation Processor Phase 1, descriptor/resource/problem
metadata slice.

## Objective

Extend the first annotation processor spike so annotated records produce
field-level resource keys, problem codes, and descriptors with basic help and
constraint metadata.

## Dependencies

- Requires COR-004 typed resource/problem/key primitives.
- Requires COR-007 validation vocabulary.
- Requires COR-011 field-key processor spike.

## Scope

Add annotation API:

- `@Required`
- `@Length(min = ..., max = ...)`
- `@DecimalRange(min = "...", max = "...")`
- `@Help(tooltip = "...", topic = "...")`

Add runtime descriptor metadata in `corusco-core`, under packages such as:

- `cz.auderis.corusco.core.meta`

Candidate public API:

- `FieldKind`
- `ConstraintKind`
- `ConstraintDescriptor`
- `FieldDescriptor`

Extend generated processor output for each `@CoruscoForm` record:

- `<RecordName>Resources`
- `<RecordName>Problems`
- `<RecordName>Descriptors`

## Required Deliverables

- Annotation and descriptor Javadocs documenting generated-code expectations.
- Processor tests proving generated descriptors contain label/help/constraint
  metadata.
- Processor failure tests for invalid constraint combinations and incompatible
  annotated types.
- Critical review against reflection policy and stable id conventions.

## Out of Scope

- Runtime validator generation.
- Full resource-bundle loading.
- `@ComboBox`, `@DateField`, `@IntRange`, `@Regex`, and `@UiAction`.
- Generated form models or behavior plans.

## Implementation Steps

1. Add source-retained help and simple constraint annotations.
2. Add small immutable descriptor records in `corusco-core`.
3. Extend processor model extraction and source generation.
4. Add focused processor tests for generated source and diagnostics.
5. Run `./gradlew clean build`.

## Acceptance Checks

- Generated descriptors contain stable field ids, resource keys, help metadata,
  and basic constraint metadata.
- Generated resource/problem ids are slash-separated stable ids, not JavaBean
  property paths.
- Invalid annotation combinations fail compilation with useful diagnostics.
- Processor still uses `javax.lang.model`, not runtime reflection.
