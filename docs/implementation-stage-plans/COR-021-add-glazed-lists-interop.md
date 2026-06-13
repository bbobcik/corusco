# COR-021 Add Glazed Lists Interop

Commit message:

```text
COR-021 Add Glazed Lists interop
```

## Roadmap Coverage

Roadmap Stage 11: Observable Collections and List Adapters, first-class Glazed
Lists interop slice.

## Objective

Add an optional interop module that adapts mature Glazed Lists `EventList<E>`
instances into Corusco `ObservableList<E>` without making Glazed Lists a
mandatory dependency of `corusco-core` or `corusco-swing`.

## Dependencies

- Requires COR-017 observable list core.
- Uses official Glazed Lists coordinates:
  `com.glazedlists:glazedlists:1.11.0`.

## Scope

Add module:

- `corusco-glazedlists`

Candidate public API:

- `GlazedObservableList<E>`
- `GlazedListsAdapters`

Required behavior:

- expose an `EventList<E>` through the `ObservableList<E>` API;
- delegate direct mutations to the wrapped `EventList`;
- translate Glazed Lists insert, delete, and update events into Corusco
  `ListChange` events;
- handle Glazed Lists reorder/reset-style events conservatively with a visible
  reset sequence if precise moves cannot be represented safely in this slice;
- release the Glazed Lists listener deterministically on close;
- preserve Corusco `batch()` delivery as one listener callback even though
  Glazed Lists remains responsible for the underlying mutation semantics;
- document Glazed Lists locking/threading expectations instead of hiding them.

## Required Deliverables

- New optional module wired into the Gradle build.
- Public Javadocs for adapter ownership, lifecycle, and event translation.
- Unit tests using `BasicEventList`.
- Example showing use of an `EventList` as a Corusco observable list, with
  method-body comments for ownership and cleanup.

## Out of Scope

- Making Glazed Lists mandatory in core or Swing.
- Adapting Corusco `ObservableList` back into a writable Glazed Lists
  `EventList`.
- Replacing COR-020 `FilteredList`.
- Swing-specific Glazed Lists model adapters beyond using the existing Corusco
  `ObservableListModel` path.

## Implementation Steps

1. Add `corusco-glazedlists` and dependency metadata.
2. Inspect the resolved Glazed Lists API and implement the adapter against the
   concrete `ListEvent` contract.
3. Add tests for mutation delegation, external source changes, updates,
   deletion old values, reorder/reset handling if available, batching, and
   close.
4. Add an example and update module documentation.
5. Run the AudEnv compact build/test recommendation.

## Acceptance Checks

- Existing projects can keep using `corusco-core` without a Glazed Lists
  dependency.
- A Glazed Lists `BasicEventList` can be observed through Corusco
  `ObservableList`.
- Event translation preserves inserted, deleted, and updated values.
- Closing the adapter stops future Corusco events.
