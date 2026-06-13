# COR-083 Add Published Examples Check

## Commit

```text
COR-083 Add published examples check
```

## Roadmap Coverage

Stage 21 preview-consumption slice covering the acceptance criterion that
examples compile against published artifacts.

## Motivation

Normal `corusco-examples` compilation uses project dependencies, which proves
the examples work in the multi-project build but does not prove that a consumer
can compile the same example sources from Maven-local artifacts. Stage 21 needs
that release-facing check before a preview tag is credible.

## Scope

- Add a published-artifact source set in `corusco-examples` that reuses main
  example sources.
- Resolve Corusco dependencies by Maven coordinates from `mavenLocal()`.
- Add a root verification task that publishes artifacts locally before
  compiling the examples against those artifacts.
- Document the verification command in the release policy and stage index.

## Non-Goals

- No remote repository publication.
- No separate runnable sample application project.
- No release tag.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `.\gradlew.bat verifyExamplesAgainstPublishedArtifacts --quiet --stacktrace`
- `git diff --check`
