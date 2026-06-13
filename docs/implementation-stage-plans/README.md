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
| [COR-022](COR-022-add-edt-observable-list-dispatcher.md) | `COR-022 Add EDT observable list dispatcher` | Stage 11, EDT dispatch slice |
| [COR-023](COR-023-add-observable-table-model-core.md) | `COR-023 Add observable table model core` | Stage 12, typed table model core slice |
| [COR-024](COR-024-add-table-selection-binding.md) | `COR-024 Add table selection binding` | Stage 12, table selection binding slice |
| [COR-025](COR-025-add-table-cell-problem-decoration.md) | `COR-025 Add table cell problem decoration` | Stage 12, table cell problem/decorator slice |
| [COR-026](COR-026-add-generated-table-columns.md) | `COR-026 Add generated table columns` | Stage 13, generated table column metadata slice |
| [COR-027](COR-027-add-generated-table-row-updaters.md) | `COR-027 Add generated table row updaters` | Stage 13, editable generated table column slice |
| [COR-028](COR-028-add-generated-table-resources.md) | `COR-028 Add generated table resources` | Stage 13, generated table resources/help slice |
| [COR-029](COR-029-add-generated-column-persistence-metadata.md) | `COR-029 Add generated column persistence metadata` | Stage 13, generated column persistence metadata slice |
| [COR-030](COR-030-add-generated-table-bindings.md) | `COR-030 Add generated table bindings` | Stage 13, generated table binding helper slice |
| [COR-031](COR-031-add-table-state-core.md) | `COR-031 Add table state core` | Stage 14, table state model and merge slice |
| [COR-032](COR-032-add-table-state-stores.md) | `COR-032 Add table state stores` | Stage 14, table state store slice |
| [COR-033](COR-033-add-table-state-controller.md) | `COR-033 Add table state controller` | Stage 14, Swing table state controller slice |
| [COR-034](COR-034-add-debounced-table-state-saves.md) | `COR-034 Add debounced table state saves` | Stage 14, table state debounce/flush slice |
| [COR-035](COR-035-add-table-header-visibility-menu.md) | `COR-035 Add table header visibility menu` | Stage 14, table header visibility menu slice |
| [COR-036](COR-036-add-table-state-schema-migration.md) | `COR-036 Add table state schema migration` | Stage 14, schema version and migration hook slice |
| [COR-037](COR-037-add-resource-lookup-core.md) | `COR-037 Add resource lookup core` | Stage 15, typed resource lookup slice |
| [COR-038](COR-038-add-help-service-core.md) | `COR-038 Add help service core` | Stage 15, core help service slice |
| [COR-039](COR-039-add-swing-help-behavior.md) | `COR-039 Add Swing help behavior` | Stage 15, Swing F1 help behavior slice |
| [COR-040](COR-040-add-tooltip-policy-core.md) | `COR-040 Add tooltip policy core` | Stage 15, core tooltip composition slice |
| [COR-041](COR-041-add-swing-tooltip-behavior.md) | `COR-041 Add Swing tooltip behavior` | Stage 15, Swing tooltip composition behavior slice |
| [COR-042](COR-042-add-status-bar-text-behavior.md) | `COR-042 Add status-bar text behavior` | Stage 15, status-bar text behavior slice |
| [COR-043](COR-043-add-accessible-text-behavior.md) | `COR-043 Add accessible text behavior` | Stage 15, accessible name/description behavior slice |
| [COR-044](COR-044-add-table-header-tooltips.md) | `COR-044 Add table header tooltips` | Stage 15, table header tooltip behavior slice |
| [COR-045](COR-045-add-table-cell-tooltips.md) | `COR-045 Add table cell tooltips` | Stage 15, table cell tooltip behavior slice |
| [COR-046](COR-046-add-detachable-value-core.md) | `COR-046 Add detachable value core` | Stage 16, detachable value foundation slice |
| [COR-047](COR-047-add-loadable-observable-list.md) | `COR-047 Add loadable observable list` | Stage 16, loadable observable-list slice |
| [COR-048](COR-048-add-detachable-lifecycle-scope.md) | `COR-048 Add detachable lifecycle scope` | Stage 16, attach/detach lifecycle integration slice |
| [COR-049](COR-049-add-master-detail-loadable-value.md) | `COR-049 Add master-detail loadable value` | Stage 16, master-detail loadable value slice |
| [COR-050](COR-050-add-async-task-core.md) | `COR-050 Add async task core` | Stage 17, core async task service slice |
| [COR-051](COR-051-add-swing-task-callbacks.md) | `COR-051 Add Swing task callbacks` | Stage 17, Swing EDT callback delivery slice |
| [COR-052](COR-052-add-generation-counter-helper.md) | `COR-052 Add generation counter helper` | Stage 17, stale-result generation counter helper slice |
| [COR-053](COR-053-add-async-field-validation.md) | `COR-053 Add async field validation` | Stage 17, async field validation API slice |
| [COR-054](COR-054-add-swing-busy-overlay.md) | `COR-054 Add Swing busy overlay` | Stage 17, Swing `JLayer` busy overlay slice |
| [COR-055](COR-055-add-dialog-result-and-form-controller.md) | `COR-055 Add dialog result and form controller` | Stage 18, dialog result and form controller foundation slice |
| [COR-056](COR-056-add-dirty-cancel-confirmation.md) | `COR-056 Add dirty-cancel confirmation` | Stage 18, dirty-cancel confirmation hook slice |
| [COR-057](COR-057-add-dialog-keyboard-bindings.md) | `COR-057 Add dialog keyboard bindings` | Stage 18, ESC and default-button handling slice |
| [COR-058](COR-058-add-dialog-validation-summary.md) | `COR-058 Add dialog validation summary` | Stage 18, validation summary and focus-first-problem slice |
| [COR-059](COR-059-add-dialog-lifecycle-scope.md) | `COR-059 Add dialog lifecycle scope` | Stage 18, dialog lifecycle integration slice |
| [COR-060](COR-060-commit-dialog-active-editors.md) | `COR-060 Commit dialog active editors` | Stage 18, active-editor commit hardening slice |
| [COR-061](COR-061-add-swing-mvp-tester-core.md) | `COR-061 Add Swing MVP tester core` | Stage 19, tester shell and component lookup slice |
| [COR-062](COR-062-add-tester-command-invocation.md) | `COR-062 Add tester command invocation` | Stage 19, action invocation by `ActionKey` slice |
| [COR-063](COR-063-add-tester-field-input-helpers.md) | `COR-063 Add tester field input helpers` | Stage 19, field input helper slice |
| [COR-064](COR-064-add-tester-table-selection-helpers.md) | `COR-064 Add tester table selection helpers` | Stage 19, table selection helper slice |
| [COR-065](COR-065-add-tester-problem-assertions.md) | `COR-065 Add tester problem assertions` | Stage 19, problem assertion helper slice |
| [COR-066](COR-066-add-tester-behavior-assertions.md) | `COR-066 Add tester behavior assertions` | Stage 19, behavior-installed assertion helper slice |
| [COR-067](COR-067-add-tester-table-state-assertions.md) | `COR-067 Add tester table-state assertions` | Stage 19, table-state assertion helper slice |
| [COR-068](COR-068-add-generated-source-test-helpers.md) | `COR-068 Add generated-source test helpers` | Stage 19, generated-source test helper slice |
| [COR-069](COR-069-add-readme-quickstart.md) | `COR-069 Add README quickstart` | Stage 20, README quickstart slice |
| [COR-070](COR-070-add-architecture-overview.md) | `COR-070 Add architecture overview` | Stage 20, architecture overview slice |
| [COR-071](COR-071-add-annotation-reference.md) | `COR-071 Add annotation reference` | Stage 20, annotation reference slice |
| [COR-072](COR-072-add-behavior-authoring-guide.md) | `COR-072 Add behavior authoring guide` | Stage 20, behavior authoring guide slice |
| [COR-073](COR-073-add-form-model-guide.md) | `COR-073 Add form model guide` | Stage 20, form model guide slice |
| [COR-074](COR-074-add-table-guide.md) | `COR-074 Add table guide` | Stage 20, table guide slice |
| [COR-075](COR-075-add-command-action-guide.md) | `COR-075 Add command action guide` | Stage 20, command/action guide slice |
| [COR-076](COR-076-add-dialog-guide.md) | `COR-076 Add dialog guide` | Stage 20, dialog guide slice |
| [COR-077](COR-077-add-testing-guide.md) | `COR-077 Add testing guide` | Stage 20, testing guide slice |
| [COR-078](COR-078-add-generated-code-examples-guide.md) | `COR-078 Add generated code examples guide` | Stage 20, generated code examples slice |
| [COR-079](COR-079-add-customer-management-example.md) | `COR-079 Add customer management example` | Stage 20, integrated customer-management example slice |
| [COR-080](COR-080-add-preview-publication-artifacts.md) | `COR-080 Add preview publication artifacts` | Stage 21, local Maven publication and documentation artifacts slice |
| [COR-081](COR-081-add-release-policy-and-changelog.md) | `COR-081 Add release policy and changelog` | Stage 21, semantic versioning, compatibility policy, and changelog slice |
| [COR-082](COR-082-add-jpms-module-descriptors.md) | `COR-082 Add JPMS module descriptors` | Stage 21, JPMS descriptors and module-name contract slice |

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
