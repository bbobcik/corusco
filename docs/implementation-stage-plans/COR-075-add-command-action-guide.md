# COR-075 Add Command Action Guide

## Commit

```text
COR-075 Add command action guide
```

## Roadmap Coverage

Stage 20 documentation slice covering typed command/action metadata,
presenter-owned command state, generated `@UiAction` descriptors, Swing action
adapters, command behaviors, and testing patterns.

## Motivation

Commands are implemented across `corusco-core`, `corusco-swing`,
`corusco-annotations`, generated processor output, and examples. The intended
boundary is important: annotations generate descriptors, presenters create and
own commands, and Swing adapters project commands into controls. Documenting
that boundary reduces the risk of later runtime reflection, duplicated Swing
handlers, or generated code that tries to invoke presenter methods by name.

## Scope

- Add `docs/commands.md`.
- Link the guide from `README.md`, quickstart, architecture overview, behavior
  guide, annotation reference, and the implementation stage index.
- Keep the guide aligned with `CommandExample`,
  `GeneratedActionMetadataExample`, `CommandBehaviors`, `SwingActionAdapter`,
  and the current `@UiAction` processor limits.

## Non-Goals

- No production code changes.
- No new command wiring generation.
- No menu or toolbar layout model.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `git diff --check`
