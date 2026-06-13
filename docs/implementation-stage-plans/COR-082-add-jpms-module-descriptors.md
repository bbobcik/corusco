# COR-082 Add JPMS Module Descriptors

## Commit

```text
COR-082 Add JPMS module descriptors
```

## Roadmap Coverage

Stage 21 JPMS slice covering `module-info.java` descriptors where the dependency
graph supports them and documentation of the preview module-name contract.

## Motivation

Stage 21 asks for JPMS descriptors where appropriate. The library artifacts are
now publishable and have clear package boundaries, so modules with stable
dependencies can declare module descriptors before the preview tag. The Glazed
Lists interop artifact depends on an upstream jar that has only a derived
automatic module name, so it receives a stable automatic module name in the jar
manifest until that dependency edge can be made fully explicit.

## Scope

- Add JPMS descriptors for `corusco-core`, `corusco-swing`,
  `corusco-annotations`, `corusco-processor`, and `corusco-test`.
- Add stable `Automatic-Module-Name` manifest entries for all published jars,
  including `corusco-glazedlists`.
- Declare the annotation processor service provider in the processor module.
- Document preview module names and remaining JPMS limits.
- Keep examples on the classpath as a non-published consumer.

## Non-Goals

- No explicit module descriptor for `corusco-examples`.
- No explicit module descriptor for `corusco-glazedlists` while upstream Glazed
  Lists exposes only a derived automatic module name.
- No release tag.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `.\gradlew.bat verifyMavenLocalPublication --quiet --stacktrace`
- `.\gradlew.bat verifyPublishedModuleMetadata --quiet --stacktrace`
- `git diff --check`
