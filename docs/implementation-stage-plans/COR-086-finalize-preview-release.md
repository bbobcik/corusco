# COR-086 Finalize Preview Release

## Commit

```text
COR-086 Finalize preview release
```

## Roadmap Coverage

Stage 21 final preview-release slice covering version finalization, changelog
finalization, and the `v0.1.0-preview` tag.

## Motivation

The preview release gates are now present and passing. The remaining roadmap
item is to turn the local preview artifact set from a snapshot line into the
first preview version and tag that exact commit.

## Scope

- Change project version from `0.1.0-SNAPSHOT` to `0.1.0-preview`.
- Update consumer documentation to use preview coordinates.
- Finalize `CHANGELOG.md` for `0.1.0-preview`.
- Run the preview release readiness gate.
- Create Git tag `v0.1.0-preview` after the release commit.

## Non-Goals

- No remote repository publication.
- No push.
- No binary compatibility plugin.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `.\gradlew.bat verifyPreviewReleaseReadiness --quiet --stacktrace`
- `git diff --check`
- `git tag --list v0.1.0-preview`
