# COR-078 Add Generated Code Examples Guide

## Commit

```text
COR-078 Add generated code examples guide
```

## Roadmap Coverage

Stage 20 documentation slice covering generated form metadata, form models,
view contracts, behavior plans, table companions, action descriptors, example
classes, and generated-code review expectations.

## Motivation

The examples module already compiles generated form, table, and action
artifacts, but the relationship between annotated source, generated companions,
runtime examples, and generated-source tests is spread across several guides.
This stage adds a generated-code examples guide and fixes a readability issue
found while reviewing generated form-model output.

## Scope

- Add `docs/generated-code-examples.md`.
- Link the guide from `README.md`, quickstart, architecture overview,
  annotation reference, testing guide, and the implementation stage index.
- Keep the guide aligned with generated source under `corusco-examples`,
  `GeneratedMetadataExample`, `GeneratedFormModelExample`,
  `GeneratedViewPlanExample`, `GeneratedTableColumnsExample`, and
  `GeneratedActionMetadataExample`.
- Fix generated form-model constructor indentation and add processor test
  coverage for the readable source shape.

## Non-Goals

- No new generated artifact kind.
- No generated method invocation glue.
- No native dialog, menu, or toolbar generation.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `git diff --check`
