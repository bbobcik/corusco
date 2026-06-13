# COR-076 Add Dialog Guide

## Commit

```text
COR-076 Add dialog guide
```

## Roadmap Coverage

Stage 20 documentation slice covering modal form-dialog controller semantics,
typed dialog results, OK/Apply/Cancel commands, dirty-cancel confirmation,
keyboard handling, validation summary, active-editor commit, and dialog
lifecycle ownership.

## Motivation

Dialog support is intentionally split between Swing-free result types,
EDT-bound controller logic, small Swing bindings, and examples. The guide needs
to make that split explicit so later work does not turn `FormDialog` into a
native window factory, duplicate cancel/commit logic in button handlers, or
introduce reflective component lookup for validation focus.

## Scope

- Add `docs/dialogs.md`.
- Link the guide from `README.md`, quickstart, architecture overview, form
  guide, command/action guide, and the implementation stage index.
- Keep the guide aligned with `FormDialogExample`,
  `DirtyCancelDialogExample`, `DialogValidationExample`,
  `DialogKeyboardExample`, `DialogLifecycleExample`, and
  `DialogActiveEditorExample`.

## Non-Goals

- No production code changes.
- No native `JDialog` factory.
- No generated dialog shell.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `git diff --check`
