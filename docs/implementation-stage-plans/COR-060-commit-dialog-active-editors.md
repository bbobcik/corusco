# COR-060 Commit dialog active editors

## Commit Message

```text
COR-060 Commit dialog active editors
```

## Roadmap Slice

Roadmap Stage 18: Modal Dialog Framework, active-editor commit for formatted
fields, spinners, and tables.

## Context

`FormDialog` already calls `SwingEditors.commitActiveEditor(root)` before OK
and Apply, but the helper needs explicit coverage for the editor families named
in the roadmap acceptance criteria. In particular, table editing should be
stopped even when the current focus owner is unavailable in headless tests or
platform-specific focus transitions, and spinner commits should use the spinner
API rather than relying only on the nested formatted text field.

## Scope

- Harden `SwingEditors` active-editor commit behavior.
- Commit focused formatted text fields.
- Commit focused spinner editors through `JSpinner.commitEdit()`.
- Stop active table cell editors under the dialog root even when focus is not
  currently inside the editor component.
- Add focused tests and an example with method-body comments.

## Out Of Scope

- Native `JDialog` construction/display helpers.
- New editor adapters beyond the Swing editor types named by Stage 18.
- Validation summary or lifecycle changes.
- Swing MVP test harness APIs from Stage 19.

## Implementation Steps

1. Refine `SwingEditors` to separate focused editor commit from table editor
   traversal.
2. Add tests for formatted fields, spinners, accepting and rejecting table
   editors, outside-root focus, and EDT/null contracts.
3. Add an example showing why dialog commit must flush table editors before
   creating a result.
4. Run test/build checks and commit the slice.

## Acceptance Checks

- `FormDialog` continues to use the shared active-editor commit helper.
- Invalid focused formatted fields or spinners block commit.
- Active table editors below the root are stopped before OK/Apply.
- A rejecting table editor blocks commit and leaves the dialog open.
- No reflection, JavaBeans, or property-path APIs are introduced.
