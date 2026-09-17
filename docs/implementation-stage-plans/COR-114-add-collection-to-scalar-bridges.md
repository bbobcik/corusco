# COR-114 Add bridge between observable collections and scalars

## Roadmap Coverage

Post-roadmap collection/value composition slice.

## Outcome

Add typed derived, item, matching, and master-detail value bridges for
observable readable collections while preserving lifecycle ownership and
precise collection-change propagation.

## Acceptance Checks

- Collection-derived values update deterministically.
- Item and matching values handle insertion, removal, replacement, and empty
  matches.
- Master-detail values detach and close without retaining obsolete listeners.
- Existing collection, Swing adapter, and Glazed Lists tests pass.
