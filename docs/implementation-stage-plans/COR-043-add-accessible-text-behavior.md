# COR-043 Add accessible text behavior

## Commit Message

```text
COR-043 Add accessible text behavior
```

## Roadmap Slice

Roadmap Stage 15: Help, Tooltips, Resources, and Accessibility, accessible
name/description behavior slice.

## Context

Stage 15 now has resource lookup, help dispatch, F1 behavior, composed
tooltips, and focus-scoped status text. Components still need a reusable way to
publish accessible names and descriptions derived from the same generated
descriptors and resource keys used for labels and tooltips.

## Scope

- Add Swing binding support for accessible name and accessible description.
- Add behavior factory methods for direct text values.
- Add a descriptor/resource-aware behavior helper for field descriptors.
- Restore previous accessible text when the binding is closed.
- Add tests for direct binding, descriptor/resource behavior, missing optional
  description text, and cleanup.
- Add an example showing generated-style resource keys feeding accessibility.

## Out Of Scope

- Table header accessible text behavior.
- Table cell accessible text behavior.
- Generated behavior-plan emission of accessibility behavior.

## Implementation Steps

1. Add `BindingFactory.accessibleText(...)`.
2. Add `StandardBehaviorKeys.ACCESSIBLE_TEXT`.
3. Add `StandardBehaviors.accessibleText(...)` overloads.
4. Add tests for direct binding and behavior-based descriptor/resource lookup.
5. Add a focused example with method-body comments.
6. Run test/build checks and commit the slice.

## Acceptance Checks

- Accessible name and description are set on install.
- Previous accessible name and description are restored on close.
- Field descriptor behavior derives name from `labelKey` and description from
  optional `tooltipKey`.
- Missing optional description resources result in blank description rather
  than failure.
- No reflection, JavaBeans, or property-path APIs are introduced.
