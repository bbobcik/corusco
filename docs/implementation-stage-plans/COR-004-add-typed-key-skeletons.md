# COR-004 Add Typed Key Skeletons

Commit message:

```text
COR-004 Add typed key skeletons
```

## Roadmap Coverage

Roadmap Stage 2: typed identity portion of Typed Keys and Problem Model.

## Objective

Introduce strongly typed identity objects for fields, text fields, resources,
help topics, actions, and components before the problem model and annotation
processor depend on them.

## Dependencies

- Requires COR-001.
- Can use COR-002/COR-003 if helpful, but should not depend on observable values.

## Scope

Add typed keys in `corusco-core`, under a package such as
`cz.auderis.corusco.core.key`.

Candidate public API:

- `FieldKey<O, T>`
- `TextFieldKey<O, T>`
- `ResourceKey<T>`
- `HelpTopic`
- `ActionKey`
- `ComponentKey<C>`

Recommended shared key properties:

- Stable string id for serialization, diagnostics, resource lookup, and
  generated code internals.
- Optional owner type where it improves diagnostics and type relationships.
- Optional value type where it avoids unsafe casts.
- Human-readable `toString()` for logs and assertion messages.
- Equality and hash code semantics that are explicit and tested.

## Required Deliverables

- New code with Javadoc: every public key type and factory must document id
  stability, type parameters, equality, and intended use by generated code.
- Tests: cover equality, hash code, diagnostics, owner/value typing, null or
  invalid ids, and representative generated-style usage.
- Examples: add simple examples for field, text field, resource, action, help,
  and component keys. Revisit earlier examples to use typed keys where doing so
  improves the demonstrated API path.

## Out of Scope

- Problem model.
- Generated key classes.
- Runtime annotation scanning.
- Reflection-based property lookup.
- Resource bundle loading.
- Swing-bound `ComponentKey<JComponent>` constraints in core. Keep the generic
  key Swing-free.

## Implementation Steps

1. Define the smallest useful key API and concrete final key types.
2. Decide whether a shared internal/base key type is warranted.
3. Add static factory methods for hand-written tests and examples.
4. Document that ids are boundary strings, not public field-path contracts.
5. Add generated-style key examples under `corusco-examples`.
6. Add tests for equality, typing, id preservation, and diagnostics.
7. Run `./gradlew clean build`.

## Acceptance Checks

- Field keys are typed by owner and value.
- Text field keys preserve the field key type relationship.
- Resource keys are typed by resource value type.
- Action, help, and component identities do not require Swing dependencies.
- No public API accepts arbitrary property-path strings for field identity.
- Tests show that same/different ids and types behave as documented.
- Public key APIs have Javadoc.
- Examples show simple generated-style key usage.

## Review Focus

- Key objects are small and stable enough for generated code to emit later.
- String ids are contained at identity/resource boundaries.
- The design does not force runtime reflection into later stages.
