# COR-085 Add Preview Release Readiness Gate

## Commit

```text
COR-085 Add preview release readiness gate
```

## Roadmap Coverage

Stage 21 release-readiness slice covering generated Javadoc readability checks
and a single aggregate local preview-release gate.

## Motivation

Stage 21 now has individual checks for publishing, module metadata, examples
against published artifacts, and API/package review. The remaining release
readiness gap is operational: preview tagging should use one explicit gate that
combines those checks and verifies that generated Javadoc output is present for
the reviewed published package surface.

## Scope

- Add a Gradle task that verifies generated Javadocs contain an index and
  package pages for the reviewed preview package surface.
- Add an aggregate `verifyPreviewReleaseReadiness` task for the local preview
  release checklist.
- Document the aggregate gate in the README and release policy.
- Add the stage plan to the implementation index and changelog.

## Non-Goals

- No release tag.
- No remote publication.
- No binary compatibility plugin.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `.\gradlew.bat verifyPreviewReleaseReadiness --quiet --stacktrace`
- `git diff --check`
