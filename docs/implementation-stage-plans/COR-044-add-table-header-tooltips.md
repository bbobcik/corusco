# COR-044 Add table header tooltips

## Commit Message

```text
COR-044 Add table header tooltips
```

## Roadmap Slice

Roadmap Stage 15: Help, Tooltips, Resources, and Accessibility, table header
tooltip behavior slice.

## Context

Generated table columns already expose typed `ColumnDescriptor` metadata with
header and tooltip resource keys. Stage 15 now has core resource lookup and
tooltip composition for component fields; table headers still need a Swing
binding that resolves column tooltip resources as the mouse moves across header
cells.

## Scope

- Add a Swing table-header tooltip binding.
- Resolve tooltip text through `ColumnDescriptor.tooltipKey()` and `Resources`.
- Convert visible header columns back to model indices so reordered/hidden
  columns still resolve the correct descriptor.
- Restore the previous header tooltip on close.
- Add focused tests and an example.

## Out Of Scope

- Table cell tooltip behavior.
- Header help/F1 dispatch.
- HTML tooltip rendering.
- Generated behavior-plan emission for table headers.

## Implementation Steps

1. Add `TableHeaderTooltipBinding`.
2. Add tests for descriptor lookup, missing optional tooltip, reordered view
   columns, and cleanup.
3. Add a small example using generated table column descriptors and resources.
4. Run test/build checks and commit the slice.

## Acceptance Checks

- Header hover shows the resource text for the hovered column.
- Missing tooltip resources clear the header tooltip.
- View-to-model conversion works after columns are reordered.
- Closing the binding removes listeners and restores the previous header
  tooltip.
- No reflection, JavaBeans, or property-path APIs are introduced.
