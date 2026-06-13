# COR-002 Add Lifecycle Subscriptions

Commit message:

```text
COR-002 Add lifecycle subscriptions
```

## Roadmap Coverage

Roadmap Stage 1: lifecycle portion of Core Lifecycle, Values, and
Subscriptions.

## Objective

Add the small lifecycle API used by values, bindings, behaviors, generated
plans, and tests to dispose listener registrations and owned resources
deterministically.

## Dependencies

- Requires COR-001.

## Scope

Add lifecycle primitives in `corusco-core`, under a package such as
`cz.auderis.corusco.core.lifecycle`.

Candidate public API:

- `Disposable`
- `Subscription`
- `SubscriptionScope`

Recommended contracts:

- `Disposable` should be closeable without checked exceptions.
- `Subscription` should represent a listener or callback registration.
- Closing a subscription must be idempotent.
- `SubscriptionScope` should own multiple subscriptions or disposables and close
  them deterministically.
- Closing a scope should close children in reverse registration order unless an
  implementation reason says otherwise.
- Closing a scope must continue closing remaining children if one child fails,
  then surface failure predictably.

## Required Deliverables

- New code with Javadoc: `Disposable`, `Subscription`, `SubscriptionScope`, and
  any helpers must document idempotency, close ordering, ownership, and exception
  behavior.
- Tests: cover normal closure, repeated closure, close ordering, listener-style
  cleanup, add-after-close behavior, and failure handling.
- Examples: add a small lifecycle example showing scoped listener/resource
  cleanup. Revisit the bootstrap example only if package or module conventions
  change.

## Out of Scope

- Observable values.
- Swing EDT enforcement.
- Binding or behavior scopes.
- Weak listeners.
- Thread-safe cross-thread ownership guarantees beyond documented assumptions.

## Implementation Steps

1. Define `Disposable` and `Subscription` minimal interfaces.
2. Add factory helpers only if they reduce repeated test boilerplate.
3. Implement `SubscriptionScope`.
4. Document idempotency, close ordering, and exception behavior in Javadoc.
5. Add a focused lifecycle example under `corusco-examples`.
6. Add tests for idempotent close, reverse close order, adding after close, and
   exception behavior.
7. Run `./gradlew clean build`.

## Acceptance Checks

- A subscription can be closed multiple times with one underlying cleanup.
- A scope closes each child exactly once.
- A scope closes children in the documented order.
- Closing a scope with one failing child does not skip unrelated children.
- Adding to an already closed scope has a documented, tested outcome.
- Public lifecycle APIs have Javadoc.
- A simple example demonstrates scoped cleanup.
- No Swing or annotation-processing dependency is introduced into `corusco-core`.

## Review Focus

- The API is intentionally small and does not overfit future binding behavior.
- Exception handling is explicit enough that later scopes can reuse the pattern.
- Tests make listener-leak prevention behavior easy to verify.
