# COR-079 Add Customer Management Example

## Commit

```text
COR-079 Add customer management example
```

## Roadmap Coverage

Stage 20 example-application slice covering a realistic miniature customer
management flow: customer search table, editable customer dialog, address
sub-dialog, invoice lines table, async VAT validation, generated help/tooltip
metadata, persistent table state, save/reset/cancel actions, and validation
summary.

## Motivation

The Stage 20 guide set is now present, but the roadmap also asks for a
miniature business application that proves the MVR pieces compose. Existing
examples are intentionally focused. This stage adds a single integrated
headless scenario that ties the generated form/table/action metadata, dialog
controller, validation summary, table state, commands, and async validation
together without introducing a native window.

## Scope

- Add `CustomerManagementExample` under `corusco-examples`.
- Add a regression test for the returned scenario diagnostics.
- Link the example from `docs/generated-code-examples.md`.
- Keep the scenario headless and deterministic.

## Non-Goals

- No native `JDialog` or application launcher.
- No persistence backend beyond in-memory state stores.
- No new framework APIs.

## Acceptance Checks

- `audenv recommend test --project .`
- `audenv recommend build --project .`
- `.\gradlew.bat test --quiet --stacktrace`
- `.\gradlew.bat build --quiet --stacktrace`
- `git diff --check`
