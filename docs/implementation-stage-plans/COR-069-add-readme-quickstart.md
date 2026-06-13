# COR-069 Add README quickstart

## Commit Message

```text
COR-069 Add README quickstart
```

## Roadmap Slice

Roadmap Stage 20: Example Application and Documentation, README quickstart.

## Context

The root README still describes annotation processing, Swing bindings,
validation, and table support as future work even though those roadmap stages
are now implemented. Stage 20 asks for a README quickstart and documentation
that shows handwritten and generated code side by side, so the public entry
point needs to be corrected before adding deeper guides.

## Scope

- Refresh the root README so it reflects the current module capabilities.
- Add a quickstart guide that shows annotated customer records beside generated
  form/table usage.
- Point readers to compiling examples and tests as the current authoritative
  reference.
- Link the new quickstart from the stage-plan index.

## Out Of Scope

- A full example application shell.
- Architecture overview, annotation reference, and the remaining Stage 20
  guides.
- New runtime APIs.

## Implementation Steps

1. Update the root README overview, module descriptions, and documentation
   links.
2. Add `docs/quickstart.md` using existing generated customer examples.
3. Register this plan in the implementation-stage index.
4. Run recommended verification and commit the slice.

## Acceptance Checks

- README no longer claims completed features are future work.
- The quickstart includes side-by-side handwritten annotation source and
  generated-code usage.
- The guide references examples that compile in the current repository.
