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
