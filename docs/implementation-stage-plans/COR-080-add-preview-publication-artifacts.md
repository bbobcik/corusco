# COR-080 Add Preview Publication Artifacts

## Commit

```text
COR-080 Add preview publication artifacts
```

## Roadmap Coverage

Stage 21 packaging slice covering source jars, Javadoc jars, Maven publication
metadata, and local Maven publication for the library modules.

## Motivation

The roadmap's preview-release stage requires consumers to use Corusco as
ordinary published artifacts. Before module descriptors, release tagging, or
binary-compatibility checks are useful, each library module needs predictable
Maven coordinates and attached source/Javadoc artifacts. This commit makes the
current multi-project build consumable from `mavenLocal()` without publishing
the example application as a library artifact.

## Scope

- Apply Gradle Maven publishing to publishable library modules.
- Generate source jars and Javadoc jars for published modules.
- Add project-level POM metadata shared by all published artifacts.
- Add a verification task for Maven-local publication artifacts.
- Document the local publication workflow.
- Keep `corusco-examples` as a compile/test consumer, not a published module.

## Non-Goals

- No remote repository publication.
- No release tag or version finalization.
- No JPMS `module-info.java` descriptors.
- No binary-compatibility plugin yet.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `.\gradlew.bat publishToMavenLocal --quiet --stacktrace`
- `.\gradlew.bat verifyMavenLocalPublication --quiet --stacktrace`
- `git diff --check`
