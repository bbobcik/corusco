# COR-035 Add Table Header Visibility Menu

Commit message:

```text
COR-035 Add table header visibility menu
```

## Roadmap Coverage

Roadmap Stage 14: Table State Persistence, table header column visibility menu
slice.

## Objective

Add a Swing header popup menu that lets users toggle descriptor-backed table
columns by stable persistence id. The menu should reuse `TableStateController`
instead of manipulating `TableColumnModel` directly, so visibility changes keep
using the same persisted state and debounced save path as programmatic changes.

## Dependencies

- Requires COR-033 table state controller.
- Requires COR-034 debounced table state saves.

## Scope

Add `TableHeaderColumnVisibilityMenu` under `corusco-swing.table`.

Required behavior:

- install a popup trigger listener on a `JTableHeader`;
- rebuild menu items from the current descriptor and controller state;
- label items with the model's column names;
- toggle column visibility through `TableStateController`;
- keep at least one column visible by disabling the last visible column item;
- remove listeners on close.

## Required Deliverables

- Public Javadocs documenting EDT confinement, lifecycle ownership, descriptor
  labels, and persistence behavior.
- Unit tests for menu item construction, visibility toggling, last-visible
  protection, and listener cleanup.
- Example update showing lifecycle-owned installation with generated table
  bindings and comments explaining the descriptor/controller relationship.
- Stage-plan index update.

## Out of Scope

- Localized resource lookup beyond the existing table model column names.
- Header icons or custom renderers.
- Schema-version migration hooks.
- Preferences-backed end-user demo.

## Implementation Steps

1. Add this stage plan and index entry.
2. Add the header visibility menu binding.
3. Add focused EDT tests using `InMemoryTableStateStore`.
4. Update the generated table example to install the menu.
5. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- The menu contains one checkbox item per descriptor column.
- Checked state reflects current table visibility.
- Toggling an item delegates to `TableStateController`.
- The last visible column cannot be hidden through the menu.
- Closing the binding removes the header mouse listener.
