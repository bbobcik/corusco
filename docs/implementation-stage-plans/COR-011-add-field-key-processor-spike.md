# COR-011 Add Field Key Processor Spike

Commit message:

```text
COR-011 Add field key processor spike
```

## Roadmap Coverage

Roadmap Stage 8: Annotation Processor Phase 1, first metamodel slice.

## Objective

Introduce the first source-retained form annotations and an annotation processor
that generates typed field-key classes for annotated records.

## Dependencies

- Requires COR-004 typed key skeletons.
- Requires the existing `corusco-annotations` and `corusco-processor` modules.

## Scope

Add annotation API:

- `@CoruscoForm(id = "...")`
- `@TextField`
- `@CheckBox`

Add processor behavior:

- Process only `@CoruscoForm` records.
- Generate `<RecordName>Fields` in the same package.
- Generate `TextFieldKey<Owner, T>` constants for `@TextField` record
  components.
- Generate `FieldKey<Owner, Boolean>` constants for `@CheckBox` boolean
  components.
- Register the processor through `META-INF/services`.
- Add Gradle incremental annotation processor metadata.

## Required Deliverables

- Annotation Javadoc documenting supported targets, retention, and generated
  code expectations.
- Processor tests that compile sample sources and assert generated source text.
- Failure tests for invalid annotation combinations.
- Example source showing generated-style field key usage may remain as a test
  fixture for this spike.

## Out of Scope

- Generated descriptors, resources, problems, actions, form models, and
  behavior plans.
- Runtime reflection fallback.
- Non-record mutable bean support.
- Validation annotation generation.

## Implementation Steps

1. Add source-retained annotations in `corusco-annotations`.
2. Add `CoruscoAnnotationProcessor` using `javax.lang.model` APIs.
3. Generate deterministic, readable Java source without reflection.
4. Add processor service and Gradle incremental metadata files.
5. Add JavaCompiler-based processor tests.
6. Run `./gradlew clean build`.

## Acceptance Checks

- Generated field keys are type-safe and compile.
- Generated key ids use stable slash-separated tokens, not JavaBean property
  paths.
- Invalid annotation combinations fail compilation with useful diagnostics.
- Processor uses language-model APIs, not runtime reflection.
- Processor service registration and Gradle incremental metadata are present.
