# COR-045 Add table cell tooltips

## Commit Message

```text
COR-045 Add table cell tooltips
```

## Roadmap Slice

Roadmap Stage 15: Help, Tooltips, Resources, and Accessibility, table cell
tooltip behavior slice.

## Context

`COR-044` added descriptor-backed header tooltips. Body cells need the same
typed resource lookup while respecting Swing's view row/column coordinates.
This behavior is intentionally separate from validation cell decoration:
validation renderers can still show problem messages, while this binding
provides static descriptor/resource help for ordinary cell hover.

## Scope

- Add a Swing table-cell tooltip binding.
- Resolve tooltip text through `ColumnDescriptor.tooltipKey()` and `Resources`.
- Convert visible cell columns back to model indices so reordered/hidden
  columns still resolve the correct descriptor.
- Clear the tooltip outside live table cells.
- Restore the previous table tooltip on close.
- Add focused tests and an example.

## Out Of Scope

- Merging static cell help with validation/problem tooltips.
- Header tooltip behavior.
- HTML tooltip rendering.
- Generated behavior-plan emission for table cells.

## Implementation Steps

1. Add `TableCellTooltipBinding`.
2. Add tests for descriptor lookup, missing optional tooltip, reordered view
   columns, outside-cell clearing, and cleanup.
3. Add a small example using generated table column descriptors and resources.
4. Run test/build checks and commit the slice.

## Acceptance Checks

- Body-cell hover shows the resource text for the hovered column.
- Missing tooltip resources clear the table tooltip.
- View-to-model conversion works after columns are reordered.
- Hovering outside real cells clears the table tooltip.
- Closing the binding removes listeners and restores the previous table
  tooltip.
- No reflection, JavaBeans, or property-path APIs are introduced.
