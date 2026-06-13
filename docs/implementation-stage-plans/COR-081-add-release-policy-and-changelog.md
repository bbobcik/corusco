# COR-081 Add Release Policy and Changelog

## Commit

```text
COR-081 Add release policy and changelog
```

## Roadmap Coverage

Stage 21 release-governance slice covering semantic versioning policy, binary
compatibility policy, and changelog documentation.

## Motivation

The preview packaging work can now publish library artifacts locally, but the
roadmap also requires explicit rules for version interpretation, compatibility
promises, and release notes. Without those rules, consumers cannot know whether
an API change is acceptable during the preview line or how to audit a future
`v0.1.0-preview` tag.

## Scope

- Add a release policy document with preview semantic-versioning rules.
- Define the binary/source compatibility contract for runtime APIs,
  annotations, generated source, and test helpers.
- Add a changelog seeded with unreleased and `0.1.0-preview` sections.
- Link the release policy and changelog from the README.
- Update stale architecture, annotation, and testing guide limits now that
  publishing and compatibility policy are present.

## Non-Goals

- No binary compatibility plugin yet.
- No released `0.1.0-preview` version change.
- No Git tag.
- No JPMS `module-info.java` descriptors.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `git diff --check`
