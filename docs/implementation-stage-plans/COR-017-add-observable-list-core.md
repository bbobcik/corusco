# COR-017 Add Observable List Core

Commit message:

```text
COR-017 Add observable list core
```

## Roadmap Coverage

Roadmap Stage 11: Observable Collections and List Adapters, core observable
list primitive.

## Objective

Introduce a Swing-free observable list with precise change events and batch
delivery.

## Dependencies

- Requires COR-002 lifecycle subscriptions.

## Scope

Add collection APIs in `corusco-core`, under packages such as:

- `cz.auderis.corusco.core.collection`

Candidate public API:

- `ObservableList<E>`
- `ObservableArrayList<E>`
- `ListChange<E>`
- `ListChangeSet<E>`
- `ListChangeListener<E>`

Supported mutations:

- add
- add at index
- set/replace
- remove
- move
- clear
- batch mutation

## Required Deliverables

- Javadocs for event semantics, ordering, and thread expectations.
- Unit tests for precise change events, batch preservation, listener removal,
  and immutable snapshots.
- Small example showing batched list changes.

## Out of Scope

- Filtered/sorted/mapped views.
- Swing `ListModel` and `ComboBoxModel` adapters.
- EDT proxy/event dispatcher.
- Table model integration.

## Implementation Steps

1. Add immutable change event model.
2. Add observable list interface.
3. Implement `ObservableArrayList`.
4. Add tests and example.
5. Run `./gradlew clean build`.

## Acceptance Checks

- Insert/delete/update/move events are precise.
- Batch events preserve inner change order.
- Listener removal during dispatch is safe.
- Exposed snapshots cannot mutate internal storage.
