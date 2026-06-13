# COR-084 Add Preview API Audit Gates

## Commit

```text
COR-084 Add preview API audit gates
```

## Roadmap Coverage

Stage 21 API-polish slice covering public package review, preview package
surface freeze, and the no-runtime-reflection policy for runtime modules.

## Motivation

Stage 21 asks for public API names to be reviewed and package structure to be
frozen for the preview. The module descriptors document the published module
names, but the build also needs a guard against accidental package growth and
against reintroducing JavaBeans/reflection-style runtime plumbing.

## Scope

- Add a documented preview API/package review.
- Add a Gradle task that verifies the published package surface matches the
  reviewed preview package list.
- Add a Gradle task that rejects forbidden reflection/JavaBeans constructs in
  runtime modules.
- Add an aggregate preview API audit task and document it in the release
  checklist.

## Non-Goals

- No binary compatibility plugin yet.
- No type or method-level API diffing.
- No package rename in this slice.
- No release tag.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `.\gradlew.bat verifyPreviewApiAudit --quiet --stacktrace`
- `git diff --check`
