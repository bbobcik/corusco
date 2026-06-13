# COR-018 Refactor Processor Source Templates

Commit message:

```text
COR-018 Refactor processor source templates
```

## Roadmap Coverage

Roadmap Stage 8 through Stage 10 maintenance: annotation processor generated
source readability and processor maintainability.

## Objective

Refactor annotation processor source generation so generated Java is assembled
from coherent text-block templates in focused renderer code, not from long
sequences of literal `Writer.write(...)` calls inside the processor.

## Rationale

The initial processor grew quickly while Stage 8 through Stage 10 generation was
being proven. Leaving validation, metamodel extraction, file writing, and source
template text in one 1000+ line processor class would make later table, dialog,
and help/resource generation brittle.

Velocity remains the preferred direction if generated source complexity grows
enough to justify a template dependency. For the current repository size, Java
text blocks provide the same readability improvement without adding a processor
classpath dependency or template-loading failure mode.

## Scope

- Keep `CoruscoAnnotationProcessor` responsible for annotation validation,
  `javax.lang.model` inspection, and orchestration.
- Move generated-source rendering and source-file writing into a focused
  package-private collaborator.
- Move field, constraint, and action generation data into package-private spec
  objects.
- Replace literal string write sequences with named text-block templates and
  helper methods.
- Preserve generated source behavior and existing generated-source assertions.

## Required Deliverables

- Processor code split into smaller maintainable classes.
- No generated-source templates expressed as repeated `writer.write("...")`
  calls.
- Existing annotation processor tests continue to compile samples and inspect
  generated sources.
- Stage documentation records the template policy and Velocity deferral.

## Out of Scope

- Adding Apache Velocity in this stage.
- Changing generated type names or public generated API shape.
- Adding table, dialog, or help/resource generation.

## Implementation Steps

1. Extract generated-source rendering from the processor.
2. Introduce package-private source-generation spec classes.
3. Convert renderer output to text-block templates.
4. Add this stage plan to the implementation-stage index.
5. Run the compact AudEnv build/test recommendation.

## Acceptance Checks

- `CoruscoAnnotationProcessor` is no longer a monolithic source generator.
- The renderer writes only completed source files, not line-by-line literal
  strings.
- Generated sources still compile through the processor test suite.
- `./gradlew clean build` passes through the AudEnv compact invocation.
