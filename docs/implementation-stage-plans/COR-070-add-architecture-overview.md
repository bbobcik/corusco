# COR-070 Add architecture overview

## Commit Message

```text
COR-070 Add architecture overview
```

## Roadmap Slice

Roadmap Stage 20: Example Application and Documentation, architecture overview.

## Context

Stage 20 requires an architecture overview. The roadmap already describes the
target principles, and the repository now has enough implemented modules that a
new contributor needs a current guide to module boundaries, generated contracts,
lifecycle ownership, table flow, behaviors, commands, and testing support.

## Scope

- Add `docs/architecture.md` describing current module layering and data flow.
- Document compile-time generation as a typed-contract boundary, not a runtime
  reflection mechanism.
- Document lifecycle scopes, table architecture, behavior/command ownership,
  and testing strategy.
- Link the architecture overview from the README and stage-plan index.

## Out Of Scope

- Full annotation reference.
- Public API naming changes.
- JPMS or packaging work.
- New runtime APIs.

## Implementation Steps

1. Derive the overview from roadmap principles and the current module/package
   layout.
2. Add architecture documentation with a module dependency diagram.
3. Update README documentation links and the implementation-stage plan index.
4. Run recommended verification and commit the slice.

## Acceptance Checks

- The overview reflects current modules and avoids claiming unavailable release
  capabilities.
- It calls out the no-reflection/no-property-path contract boundary.
- It documents how generated classes, lifecycle scopes, tables, behaviors,
  commands, and tests fit together.
