# Implementation Stage Plans

This directory expands the roadmap into commit-sized implementation plans.

Each document describes one coherent implementation stage and names the exact
commit message that should close the stage. Commit messages use this format:

```text
COR-nnn Short imperative title
```

The `COR-nnn` sequence is implementation-order numbering, not the same as the
roadmap's Stage 0 through Stage 21 numbering. Do not renumber stages after a
commit has been created.

## Roadmap Review Notes

- The roadmap uses repository module names (`corusco-*`) and should be kept in
  sync with the stage plans when module boundaries change.
- The roadmap's high-level stages are useful milestones, but some are too broad
  for one clean commit. These plans split early work into smaller commits where
  that keeps review and rollback practical.
- Public runtime APIs should avoid runtime reflection, arbitrary public string
  contracts, and Swing dependencies in `corusco-core`.
- Swing-specific utilities and EDT checks belong in `corusco-swing`.
- Annotation processing remains out of scope until handwritten APIs are stable.

## First Set

| Stage | Commit message | Roadmap coverage |
| --- | --- | --- |
| [COR-001](COR-001-bootstrap-repository-baseline.md) | `COR-001 Bootstrap repository baseline` | Stage 0 |
| [COR-002](COR-002-add-lifecycle-subscriptions.md) | `COR-002 Add lifecycle subscriptions` | Stage 1, lifecycle part |
| [COR-003](COR-003-add-observable-value-primitives.md) | `COR-003 Add observable value primitives` | Stage 1, value part |
| [COR-004](COR-004-add-typed-key-skeletons.md) | `COR-004 Add typed key skeletons` | Stage 2, key part |
| [COR-005](COR-005-add-problem-model.md) | `COR-005 Add problem model` | Stage 2, problem part |
| [COR-006](COR-006-add-field-model-core.md) | `COR-006 Add field model core` | Stage 3, non-Swing form core |
| [COR-007](COR-007-add-validation-rule-core.md) | `COR-007 Add validation rule core` | Stage 4, non-Swing validation core |
| [COR-008](COR-008-add-basic-swing-bindings.md) | `COR-008 Add basic Swing bindings` | Stage 5 |
| [COR-009](COR-009-add-behavior-core.md) | `COR-009 Add behavior core` | Stage 6 |
| [COR-010](COR-010-add-command-action-core.md) | `COR-010 Add command action core` | Stage 7, command/action core |
| [COR-011](COR-011-add-field-key-processor-spike.md) | `COR-011 Add field key processor spike` | Stage 8, first metamodel slice |
| [COR-012](COR-012-add-generated-field-descriptors.md) | `COR-012 Add generated field descriptors` | Stage 8, descriptor/resource/problem slice |
| [COR-013](COR-013-add-remaining-field-metadata-annotations.md) | `COR-013 Add remaining field metadata annotations` | Stage 8, remaining field metadata slice |
| [COR-014](COR-014-add-generated-action-descriptors.md) | `COR-014 Add generated action descriptors` | Stage 8, action metadata slice |
| [COR-015](COR-015-add-generated-record-form-models.md) | `COR-015 Add generated record form models` | Stage 9, first generated form-model slice |
| [COR-016](COR-016-add-generated-view-contracts.md) | `COR-016 Add generated view contracts` | Stage 10, initial view/behavior plan slice |
| [COR-017](COR-017-add-observable-list-core.md) | `COR-017 Add observable list core` | Stage 11, observable list core slice |
| [COR-018](COR-018-refactor-processor-source-templates.md) | `COR-018 Refactor processor source templates` | Stage 8-10 processor maintainability slice |
| [COR-019](COR-019-add-swing-list-adapters.md) | `COR-019 Add Swing list adapters` | Stage 11, Swing list/combo adapter slice |
| [COR-020](COR-020-add-filtered-observable-list.md) | `COR-020 Add filtered observable list` | Stage 11, filtered list view slice |
| [COR-021](COR-021-add-glazed-lists-interop.md) | `COR-021 Add Glazed Lists interop` | Stage 11, Glazed Lists interop slice |

## Stage Completion Rule

A stage is complete only when:

1. It adds the planned production code with Javadoc on public APIs, including
   lifecycle, threading, ownership, and nullability expectations where relevant.
2. It adds a sufficient volume of tests for the stage's nominal behavior, edge
   cases, and regression-prone interactions. A stage should not rely on a later
   stage to prove its own contracts.
3. It adds or updates appropriate examples. Early low-level stages may use
   small focused examples; later user-facing stages should add richer examples.
   Existing examples must be revisited and refactored when a new stage changes
   the recommended usage pattern. Example methods should include a few
   well-placed body comments where they clarify ownership, cleanup, threading,
   or generated-code conventions better than method-level Javadoc alone.
4. Its acceptance checks pass.
5. AudEnv has been queried for the relevant commands. Before test commands, run
   `audenv recommend test --project .` and prefer `project-compact:` when
   present.
6. The recommended build/test commands pass. If a stage plan names a generic
   Gradle command, use the AudEnv recommendation for the local project command
   form.
7. Chatty tools such as Gradle are invoked compactly: full output goes to a
   temporary/local log, normal conversation output stays to status, concise
   failure excerpts, and the log path.
8. The resulting diff is coherent enough to review as one commit with the named
   commit message.
