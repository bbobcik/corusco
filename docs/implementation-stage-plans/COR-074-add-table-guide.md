# COR-074 Add Table Guide

## Commit

```text
COR-074 Add table guide
```

## Roadmap Coverage

Stage 20 documentation slice covering table descriptors, generated table
metadata, observable table models, Glazed Lists row sources, table selection,
table-state persistence, and current processor limits.

## Motivation

The repository has table runtime and generated table support, but the intended
composition is spread across examples, generated sources, and API Javadocs. A
newcomer should be able to understand how a generated row record becomes a
Swing `JTable`, where Glazed Lists fits, how row selection maps through sorted
views, and why persisted table state uses stable ids rather than Swing column
instances.

## Scope

- Add `docs/tables.md`.
- Link the guide from `README.md`, quickstart, annotation reference, and
  architecture overview.
- Record the stage in `docs/implementation-stage-plans/README.md`.
- Keep the guide aligned with current generated examples and current limits.

## Non-Goals

- No production code changes.
- No annotation processor behavior changes.
- No new table features beyond documenting the current API.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `git diff --check`
