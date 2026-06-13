# COR-073 Add form model guide

## Commit Message

```text
COR-073 Add form model guide
```

## Roadmap Slice

Roadmap Stage 20: Example Application and Documentation, form model guide.

## Context

Stage 20 requires a form model guide. The framework now has handwritten and
generated form model support, text-field parse state, dirty/touched tracking,
validation rule aggregation, generated validators, async field validation, and
Swing behavior/binding integration. Developers need a guide that explains the
transactional editing model and the boundary between Swing-free form state and
Swing bindings.

## Scope

- Add `docs/forms.md`.
- Document form-model responsibilities, raw text versus semantic values,
  handwritten and generated form shapes, dirty/touched state, reset and
  baseline acceptance, committability, validation, async validation, Swing
  binding boundaries, and tests.
- Link the guide from README, architecture, quickstart, annotation reference,
  and the stage-plan index.

## Out Of Scope

- Runtime API changes.
- Generated form model changes.
- Full dialog guide.
- Full command/action guide.

## Implementation Steps

1. Derive the guide from current core form/validation APIs, generated form model
   output, and examples.
2. Add form-model documentation with handwritten and generated snippets.
3. Update documentation navigation links.
4. Run recommended verification and commit the slice.

## Acceptance Checks

- The guide explains invalid intermediate input and previous semantic value
  preservation.
- It documents guarded result creation and `UncommittableFormException`.
- It covers handwritten and generated form models without claiming unsupported
  annotation-driven cross-field validation.
- It preserves the no-reflection/no-property-path contract.
