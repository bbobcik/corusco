# COR-058 Add dialog validation summary

## Commit Message

```text
COR-058 Add dialog validation summary
```

## Roadmap Slice

Roadmap Stage 18: Modal Dialog Framework, validation summary and
focus-first-problem slice.

## Context

`COR-055` through `COR-057` established form dialog results, commands,
dirty-cancel confirmation, ESC handling, and default-button wiring. Stage 18
also requires a validation summary and focus-first-problem behavior. Current
`FormModel` problems are synchronous rather than observable, so this slice uses
an explicit binding with a refresh method and a typed focus resolver supplied by
generated or hand-written dialog code.

## Scope

- Add `ProblemFocusResolver` in the Swing dialog package.
- Add `FormDialogValidationBinding` for summary label text and
  focus-first-problem behavior.
- Keep component lookup explicit through typed component/problem targets.
- Restore summary label text on close.
- Add tests and an example with method-body comments.

## Out Of Scope

- Automatic form-wide dirty/problem observation.
- Native `JDialog` factory/display helpers.
- Generated behavior-plan emission.
- Table cell scrolling/focus behavior beyond resolver hooks.
- Presenter/task lifecycle integration.

## Implementation Steps

1. Add `ProblemFocusResolver` with no-op and component-key map helpers.
2. Add `FormDialogValidationBinding` with EDT-bound refresh/focus lifecycle.
3. Add tests for empty summary, severity-ordered summary, focus resolver,
   component-target helper, missing focus targets, and close restore behavior.
4. Add a focused example showing summary refresh and first-problem focus.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- Summary label is empty when the form has no problems.
- Summary label reflects problem count and highest-severity message.
- Focus-first-problem attempts focus on the first resolvable severity-ordered
  problem.
- Missing focus targets are handled without failure.
- Closing restores the previous summary label text.
- No reflection, JavaBeans, or property-path APIs are introduced.
