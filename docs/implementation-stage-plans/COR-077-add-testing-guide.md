# COR-077 Add Testing Guide

## Commit

```text
COR-077 Add testing guide
```

## Roadmap Coverage

Stage 20 documentation slice covering runtime tests, Swing MVP tests,
generated-source compiler tests, example regression tests, AudEnv verification
commands, and current test-support limits.

## Motivation

Testing support now spans core model tests, Swing binding tests,
`SwingMvpTester`, generated-source compiler helpers, and example regression
fixtures. Without a focused guide, new tests can easily fall back to private
Swing fields, reflection, property paths, unscoped EDT access, or brittle full
generated-source comparisons.

## Scope

- Add `docs/testing.md`.
- Link the guide from `README.md`, quickstart, architecture overview, behavior
  guide, dialog guide, and the implementation stage index.
- Keep the guide aligned with `SwingMvpTester`,
  `SwingMvpTesterExample`, `SwingMvpTesterBehaviorExample`,
  `SwingMvpTesterTableExample`, `GeneratedSourceCompiler`, and current AudEnv
  command guidance.

## Non-Goals

- No production code changes.
- No new test harness APIs.
- No screenshot, visual regression, or binary compatibility tooling.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `git diff --check`
