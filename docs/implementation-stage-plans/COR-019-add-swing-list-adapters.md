# COR-019 Add Swing List Adapters

Commit message:

```text
COR-019 Add Swing list adapters
```

## Roadmap Coverage

Roadmap Stage 11: Observable Collections and List Adapters, Swing adapter
slice.

## Objective

Adapt `ObservableList<E>` to Swing `ListModel<E>` and `ComboBoxModel<E>` while
preserving precise Swing list events where Swing's event model can represent
the underlying change.

## Dependencies

- Requires COR-017 observable list core.
- Requires COR-008 Swing EDT helper conventions.

## Scope

Add Swing collection adapters under a Swing-owned package such as:

- `cz.auderis.corusco.swing.collection`

Candidate public API:

- `ObservableListModel<E>`
- `ObservableComboBoxModel<E>`

Required behavior:

- expose source list size and indexed elements;
- translate insertions to `intervalAdded`;
- translate removals and clear operations to `intervalRemoved`;
- translate replacements to `contentsChanged`;
- translate moves to the smallest affected `contentsChanged` range because
  `ListDataEvent` has no move event;
- close source subscriptions deterministically;
- document that construction and source mutations observed by the adapter must
  happen on the EDT until a later explicit EDT-dispatch adapter exists.

## Required Deliverables

- Public Javadocs for lifecycle and EDT expectations.
- Swing adapter tests for event translation, batch delivery, selection handling,
  and disposal.
- Example showing an observable list driving Swing list/combo models with
  method-body comments that explain ownership and EDT constraints.

## Out of Scope

- Filtered, sorted, or mapped observable list views.
- Background-originated list change marshalling.
- Table models.
- Generated list metadata.

## Implementation Steps

1. Add Swing collection package and adapter classes.
2. Translate every current `ListChange` variant to Swing events.
3. Add focused tests using `SwingEdt.runAndWait`.
4. Add an example demonstrating lifecycle cleanup.
5. Run the AudEnv compact build/test recommendation.

## Acceptance Checks

- `JList`-compatible adapter reports source contents and fires correct events.
- `JComboBox`-compatible adapter tracks explicit selection and clears it when
  the selected element is removed.
- Closing an adapter stops future Swing events and is idempotent.
- Off-EDT observed source changes fail fast rather than silently firing Swing
  events on the wrong thread.
