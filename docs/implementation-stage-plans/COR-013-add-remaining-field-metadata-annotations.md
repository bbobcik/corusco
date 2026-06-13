# COR-013 Add Remaining Field Metadata Annotations

Commit message:

```text
COR-013 Add remaining field metadata annotations
```

## Roadmap Coverage

Roadmap Stage 8: Annotation Processor Phase 1, remaining field annotation
metadata except actions.

## Objective

Extend generated field metadata beyond text fields and checkboxes to cover
combo boxes, date fields, integer ranges, and regular-expression constraints.

## Dependencies

- Requires COR-011 field-key processor spike.
- Requires COR-012 generated resource/problem/descriptor metadata.

## Scope

Add annotation API:

- `@ComboBox`
- `@DateField`
- `@IntRange`
- `@Regex`

Extend runtime descriptor enums:

- `FieldKind.COMBO_BOX`
- `FieldKind.DATE`
- `ConstraintKind.INT_RANGE`
- `ConstraintKind.REGEX`

Extend processor output:

- Generate `FieldKey<Owner, T>` constants for combo boxes.
- Generate `TextFieldKey<Owner, LocalDate>` constants for date fields.
- Generate descriptor metadata for combo/date fields.
- Generate problem/constraint metadata for integer ranges and regex patterns.

## Required Deliverables

- Javadocs for new annotations and metadata enum values.
- Processor tests for generated keys/descriptors and invalid combinations.
- Example update showing generated combo/date/range/regex metadata.

## Out of Scope

- `@UiAction` and generated action descriptors.
- Runtime validator installation.
- Combo-box option-source generation.
- Date parsing/format policy generation.

## Implementation Steps

1. Add source-retained annotations.
2. Extend core metadata enums and constraint factories.
3. Extend processor field classification and metadata validation.
4. Expand generated metadata example and tests.
5. Run `./gradlew clean build`.

## Acceptance Checks

- Generated metadata remains type-safe and compiles in downstream modules.
- Invalid field/constraint annotation combinations fail compilation.
- Generated ids remain stable slash-separated ids.
- Processor still uses language-model APIs only.
