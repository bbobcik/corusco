# COR-071 Add annotation reference

## Commit Message

```text
COR-071 Add annotation reference
```

## Roadmap Slice

Roadmap Stage 20: Example Application and Documentation, annotation reference.

## Context

Stage 20 requires an annotation reference. The quickstart shows a happy path,
but developers still need a precise description of each supported annotation,
its attributes, generated companions, processor validation rules, and current
limits. The reference must match the current annotation API and processor rather
than the broader future roadmap.

## Scope

- Add `docs/annotations.md`.
- Document stable ID rules, form annotations, table annotations, action
  annotations, generated names, recommended patterns, and current limits.
- Link the reference from the README and implementation-stage index.

## Out Of Scope

- Javadoc generation.
- Annotation processor behavior changes.
- New annotations or runtime APIs.
- Full form/table/command guides.

## Implementation Steps

1. Derive the reference from current annotation declarations and processor
   validation code.
2. Add examples that align with existing generated examples.
3. Update README documentation links and the stage-plan index.
4. Run recommended verification and commit the slice.

## Acceptance Checks

- The reference documents every public annotation in `corusco-annotations`.
- Attribute defaults and type restrictions match current source code.
- The guide states that annotations are compile-time inputs and not runtime
  reflection contracts.
- The reference does not claim unavailable roadmap features as complete.
