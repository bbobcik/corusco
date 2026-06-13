# COR-041 Add Swing tooltip behavior

## Commit Message

```text
COR-041 Add Swing tooltip behavior
```

## Roadmap Slice

Roadmap Stage 15: Help, Tooltips, Resources, and Accessibility, Swing
tooltip composition behavior slice.

## Context

COR-040 introduced a toolkit-neutral `TooltipPolicy`. Existing Swing tooltip
support only binds validation messages and uses insertion order directly. Swing
components now need a behavior that consumes the core policy, composes
validation, disabled reason, static help, and F1 hint text consistently, and
cleans up after itself.

## Scope

- Add a composed Swing tooltip binding in `BindingFactory`.
- Add `StandardBehaviors.composedTooltip(...)` for behavior-based installation.
- Route legacy `validationTooltip(...)` through the composed policy so problem
  ordering matches the core contract.
- Use one shared tooltip behavior key so multiple tooltip decorators cannot
  fight over one component.
- Restore the component's previous tooltip when the binding is closed.
- Add tests and update the behavior example.

## Out Of Scope

- HTML tooltip rendering.
- Status-bar text behavior.
- Accessible description/name behavior.
- Table header and table cell tooltip adapters.

## Implementation Steps

1. Add a general tooltip behavior key and keep validation tooltip as an alias.
2. Add composed tooltip binding overloads that subscribe to problem and disabled
   reason values.
3. Add behavior factory methods that delegate to the composed binding.
4. Update tests and examples to exercise generated static help plus dynamic
   problems and disabled reason text.
5. Run test/build checks and commit the slice.

## Acceptance Checks

- Swing tooltip text follows core `TooltipPolicy` ordering.
- Validation-only tooltip behavior uses severity ordering and restores the
  previous tooltip on close.
- Dynamic disabled reason changes update the tooltip immediately.
- Installing two tooltip decorators on one component fails through behavior
  conflict detection.
- The example contains method-body comments explaining generated help/resource
  use and cleanup.
