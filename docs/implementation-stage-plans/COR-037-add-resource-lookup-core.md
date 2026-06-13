# COR-037 Add Resource Lookup Core

Commit message:

```text
COR-037 Add resource lookup core
```

## Roadmap Coverage

Roadmap Stage 15: Help, Tooltips, Resources, and Accessibility, core resource
lookup slice.

## Objective

Introduce a Swing-free resource lookup boundary for generated
`ResourceKey<T>` descriptors. Runtime code should be able to resolve typed
resources from simple maps today while leaving room for bundle-backed,
localized, or dynamic resource providers in later commits.

## Dependencies

- Requires existing typed `ResourceKey<T>` keys.

## Scope

Add `corusco-core.resource` APIs:

- `Resources`
- `MapResources`
- `ResourceException`

Required behavior:

- resolve optional typed values by `ResourceKey<T>`;
- return fallback values without throwing for absent resources;
- throw clear exceptions for required missing resources;
- reject values whose runtime type does not match the key value type;
- provide immutable map-backed resources for tests, examples, and generated
  metadata demos.

## Required Deliverables

- Public Javadocs describing type checks, missing-resource behavior, and
  immutability.
- Unit tests for found values, missing values, fallback values, required
  lookups, type mismatch, and immutable snapshots.
- Example showing generated resource-key descriptors resolved through
  `Resources` with method-body comments.
- Stage-plan index update.

## Out of Scope

- ResourceBundle integration.
- Locale switching.
- Tooltip composition.
- Help service behavior.
- Annotation processor changes.

## Implementation Steps

1. Add this stage plan and index entry.
2. Add the core resource package and typed lookup API.
3. Add immutable map-backed resource implementation.
4. Add tests for lookup semantics and type safety.
5. Add/update examples demonstrating generated key lookup.
6. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- Resource lookups are typed by `ResourceKey<T>`.
- Missing resources can be handled with optional or fallback APIs.
- Required missing resources fail with `ResourceException`.
- Type mismatches fail with `ResourceException`.
- `MapResources` takes an immutable snapshot of input mappings.
