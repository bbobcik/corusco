# COR-020 Add Filtered Observable List

Commit message:

```text
COR-020 Add filtered observable list
```

## Roadmap Coverage

Roadmap Stage 11: Observable Collections and List Adapters, filtered view
slice.

## Objective

Introduce a read-only `FilteredList<E>` view over an `ObservableList<E>` that
keeps a visible subset synchronized with source mutations and emits view-level
change events.

## Dependencies

- Requires COR-017 observable list core.

## Scope

Add collection API in `corusco-core`:

- `FilteredList<E>`

Required behavior:

- expose only source elements accepted by a `Predicate<? super E>`;
- emit visible insertions when matching source elements are inserted;
- emit visible removals when matching source elements are removed or no longer
  match after replacement;
- emit visible replacements when a matching element is replaced by another
  matching element;
- emit visible moves when a matching source move changes the visible order;
- support predicate replacement by publishing the minimal supported reset event
  pair for this early slice;
- close the source subscription deterministically.

## Design Notes

The filtered view is read-only. Direct mutation methods throw
`UnsupportedOperationException`; callers should mutate the source list so source
indices, filtering, and later sorted/mapped transformations remain coherent.

## Required Deliverables

- Public Javadocs describing lifecycle, read-only semantics, and synchronous
  dispatch.
- Unit tests for visible insert/remove/replace/move behavior, predicate
  replacement, listener disposal, and unsupported direct mutations.
- Example showing a source list and filtered view with method-body comments that
  clarify ownership and cleanup.

## Out of Scope

- Sorted or mapped views.
- Writable filtered views.
- Predicate changes expressed as a fine-grained diff.
- EDT dispatch or Swing adapters beyond reusing already implemented adapters.
- Glazed Lists interop; Stage 11 should add that as an explicit first-class
  adapter slice rather than hiding it inside this filtered-view primitive.

## Implementation Steps

1. Add `FilteredList<E>` to `corusco-core`.
2. Maintain a visible snapshot and translate source changes into visible
   changes.
3. Add focused unit tests.
4. Add an example.
5. Run the AudEnv compact build/test recommendation.

## Acceptance Checks

- View contents stay synchronized after source insert, remove, replace, move,
  clear, and batch operations.
- Emitted change indices are relative to the filtered view, not the source.
- Closing the view stops future events and is idempotent.
- Direct mutation methods fail fast with `UnsupportedOperationException`.
