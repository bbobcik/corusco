# COR-014 Add Generated Action Descriptors

Commit message:

```text
COR-014 Add generated action descriptors
```

## Roadmap Coverage

Roadmap Stage 8: Annotation Processor Phase 1, `@UiAction` and generated
action metadata.

## Objective

Introduce source-retained action metadata annotations and generate typed action
keys, resource keys, and command `ActionDescriptor` constants.

## Dependencies

- Requires COR-010 command/action core.
- Requires COR-011 processor registration.
- Requires COR-012 generated resource metadata conventions.

## Scope

Add annotation API:

- `@UiAction`

Extend processor output:

- Process no-argument methods annotated with `@UiAction`.
- Generate `<OwnerName>Actions` beside the annotated owner type.
- Generate `ActionKey` constants.
- Generate text/tooltip `ResourceKey<String>` constants.
- Generate `ActionDescriptor` constants with optional mnemonic, accelerator,
  and selectable metadata.

## Required Deliverables

- Annotation Javadoc documenting supported method shape and generated metadata.
- Processor tests for generated action descriptors and invalid methods.
- Example update showing generated action descriptor consumption.

## Out of Scope

- Generated command instances and handler invocation.
- Async wrapping.
- Generated view behavior plans.
- Icon metadata.

## Implementation Steps

1. Add `@UiAction`.
2. Extend processor supported annotations and action extraction.
3. Generate deterministic action metadata source.
4. Add tests and example coverage.
5. Run `./gradlew clean build`.

## Acceptance Checks

- Generated action metadata uses typed `ActionKey` and `ResourceKey<String>`.
- Generated descriptors compile and preserve accelerator/mnemonic metadata.
- Invalid annotated methods fail compilation with clear diagnostics.
- Processor still uses language-model APIs only.
