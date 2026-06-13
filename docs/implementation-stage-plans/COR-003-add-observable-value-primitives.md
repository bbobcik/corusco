# COR-003 Add Observable Value Primitives

Commit message:

```text
COR-003 Add observable value primitives
```

## Roadmap Coverage

Roadmap Stage 1: value portion of Core Lifecycle, Values, and Subscriptions.

## Objective

Implement synchronous observable value primitives that can back form fields,
validation state, command enablement, generated models, and Swing bindings.

## Dependencies

- Requires COR-002 lifecycle subscriptions.

## Scope

Add value APIs in `corusco-core`, under a package such as
`cz.auderis.corusco.core.value`.

Candidate public API:

- `ReadableValue<T>`
- `WritableValue<T>`
- `SimpleValue<T>`
- `DerivedValue<T>`
- `MappedValue<A, B>`
- `ChangeOrigin`
- `ValueChangeEvent<T>`

Expected behavior:

- Value mutation is synchronous.
- Listeners are notified exactly once per effective value change.
- Equal old/new values should not notify unless explicit invalidation is added
  and documented.
- Listener removal during dispatch must not corrupt dispatch.
- Derived values should update when dependencies update.
- Mapped values should make one-way derivation easy without requiring Swing.

## Required Deliverables

- New code with Javadoc: all public value APIs must document synchronous
  dispatch, equality behavior, listener ownership, null handling, and threading
  assumptions.
- Tests: cover mutation, non-mutation on equal values, listener removal during
  dispatch, subscription closure, null values, change origins, derived values,
  and mapped values.
- Examples: add simple and intermediate examples showing a `SimpleValue`, a
  derived value, and cleanup through lifecycle subscriptions. Refactor lifecycle
  examples if the value API changes the preferred cleanup pattern.

## Out of Scope

- Validation, problems, form fields, and converters.
- Asynchronous values.
- EDT confinement enforcement.
- Bidirectional Swing text binding.
- Generated code.

## Implementation Steps

1. Define listener/subscription shape on `ReadableValue<T>`.
2. Implement `SimpleValue<T>` with robust listener dispatch.
3. Add `ChangeOrigin` with enough structure for user, model, binding, generated,
   and system-originated changes.
4. Add `ValueChangeEvent<T>` as an immutable event type.
5. Implement derived or mapped values with dependency subscriptions.
6. Document equality, null, synchronous dispatch, and threading assumptions.
7. Add focused examples under `corusco-examples`.
8. Add focused unit tests.
9. Run `./gradlew clean build`.

## Acceptance Checks

- Setting a different value notifies subscribers once.
- Setting an equal value does not notify.
- Subscribers receive old value, new value, origin, and source value.
- Closing a subscription stops future notifications.
- Removing a subscriber while dispatching does not skip or duplicate unrelated
  subscribers.
- Derived or mapped values update after dependency changes.
- Public value APIs have Javadoc.
- Examples demonstrate simple and derived value usage.
- No Swing dependency is introduced into `corusco-core`.

## Review Focus

- Listener dispatch is simple and predictable.
- Allocation and copying are reasonable for UI hot paths.
- Public naming leaves room for later invalidation or buffered value semantics.
