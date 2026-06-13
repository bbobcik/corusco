# COR-072 Add behavior authoring guide

## Commit Message

```text
COR-072 Add behavior authoring guide
```

## Roadmap Slice

Roadmap Stage 20: Example Application and Documentation, behavior authoring
guide.

## Context

Stage 20 requires a behavior authoring guide. The runtime already has behavior
keys, descriptors, phases, scopes, built-in behaviors, command behaviors, help
service integration, and tester assertions. Developers need a current guide that
explains how to compose built-ins and how to write custom behaviors without
leaking listeners or bypassing the typed generated contracts.

## Scope

- Add `docs/behaviors.md`.
- Document core behavior types, built-in behavior factories, custom behavior
  authoring, descriptor metadata, lifecycle ownership, generated behavior plans,
  and tests.
- Link the guide from README, architecture, quickstart, and the stage-plan
  index.

## Out Of Scope

- New behavior runtime APIs.
- Generated behavior plan changes.
- Full command/action guide.
- Full form-model guide.

## Implementation Steps

1. Derive the guide from current `corusco-swing` behavior APIs and examples.
2. Include a custom behavior example that restores previous Swing state.
3. Add navigation links from existing docs.
4. Run recommended verification and commit the slice.

## Acceptance Checks

- The guide documents current behavior phases, descriptors, scope ownership,
  cleanup, and built-in behavior factories.
- The custom behavior example uses EDT checks and returns a cleanup binding.
- The guide preserves the no-reflection/no-property-path contract.
- It does not claim unavailable registry or dependency-injection behavior.
