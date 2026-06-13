# COR-040 Add tooltip policy core

## Commit Message

```text
COR-040 Add tooltip policy core
```

## Roadmap Slice

Roadmap Stage 15: Help, Tooltips, Resources, and Accessibility, core
`TooltipPolicy` and tooltip composition slice.

## Context

COR-037 added typed resource lookup, COR-038 added toolkit-neutral help
dispatch, and COR-039 connected F1 to Swing behavior. The next foundation is a
single core tooltip composition policy so validation, disabled-state, static
help, and F1 hint ordering does not get duplicated across field, table,
status-bar, and accessibility adapters.

## Scope

- Core `TooltipContent` value object for dynamic/static tooltip parts.
- Core `TooltipPolicy` that composes ordered plain-text lines and optional
  newline-separated text.
- Standard composition order:
  1. most severe validation/parse/feedback problem;
  2. disabled reason;
  3. static descriptor/resource help;
  4. F1/context help indicator.
- Unit tests for ordering, empty content, blank handling, tie stability, and
  configurable help indicator text.
- Example demonstrating generated resource/help metadata feeding the core
  policy, with method-body comments for newcomers.

## Out Of Scope

- Swing tooltip installation.
- HTML tooltip rendering.
- Status-bar behavior.
- Accessibility name/description binding.
- Table header and table cell tooltip adapters.

## Implementation Steps

1. Add the core tooltip package and public API.
2. Compose lines from `ProblemSet`, disabled reason, static help, and F1
   availability.
3. Add focused core tests.
4. Add an example that combines generated table resources, generated help
   metadata, and dynamic validation feedback.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- The most severe problem is shown first, with insertion order preserved for
  same-severity ties.
- Disabled reason, static help, and F1 indicator follow the roadmap order.
- Empty content returns no tooltip.
- Blank strings are ignored.
- The example documents the intended generated-resource and generated-help
  workflow.
