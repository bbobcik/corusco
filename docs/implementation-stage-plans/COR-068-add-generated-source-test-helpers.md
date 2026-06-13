# COR-068 Add generated-source test helpers

## Commit Message

```text
COR-068 Add generated-source test helpers
```

## Roadmap Slice

Roadmap Stage 19: Swing MVP Test Harness, generated-source test helpers.

## Context

Processor tests currently embed javac setup, temporary output layout, diagnostic
collection, generated-source file reading, and line-ending assumptions directly
inside `CoruscoAnnotationProcessorTest`. That duplication makes later
annotation-processor slices harder to review and encourages large tests to
carry infrastructure details beside the behavior they prove.

The repository already has a `corusco-test` module, so the helper should live
there instead of remaining private to `corusco-processor`.

## Scope

- Add a reusable javac harness for compiling sample source files with explicit
  annotation processors.
- Add a generated-source result object with normalized source reading and
  readable snippet assertions.
- Refactor existing processor tests to use the shared helper.
- Cover the helper with focused tests and method-body comments that explain the
  generated-source workflow.

## Out Of Scope

- Processor production refactors.
- Velocity template adoption or generated-source writer changes.
- Swing component interaction helpers.
- A fluent assertion library dependency in the production `corusco-test` API.

## Implementation Steps

1. Add `GeneratedSourceCompiler` and `GeneratedSourceCompilation` to
   `corusco-test`.
2. Add helper tests using a tiny demo annotation processor.
3. Depend on `corusco-test` from `corusco-processor` tests.
4. Replace local javac and generated-source reading code in the processor test.
5. Run recommended test/build checks and commit the slice.

## Acceptance Checks

- Processor tests compile sample sources through shared helper APIs.
- Generated source assertions normalize line endings and include useful failure
  context.
- Helper code stays independent of AssertJ so it remains a plain test-support
  API.
- No reflection, JavaBeans, or property-path APIs are introduced.
