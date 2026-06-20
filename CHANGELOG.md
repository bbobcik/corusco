# Changelog

All notable changes to Corusco are recorded here. The project follows the
compatibility rules in [docs/release-policy.md](docs/release-policy.md).

## 1.1.0 - Unreleased

### Added

- Observable readable collection APIs, including sorted-set and mapped
  read-only collection views for business screens that expose ordered data
  without granting mutation rights.
- Read-only table-model and Glazed Lists bridge support for observable readable
  collections.
- `@CoruscoForm` support for abstract class sources, including generated
  immutable result implementations.
- Richer generated form metadata: presentation models, enum option descriptors,
  radio-group behavior, component-state models, dependency metadata, and
  generated behavior bindings.
- `@CoruscoForm`, `@CoruscoTable`, and `@SwingCompanionPackage` as the current
  form/table annotation model with separate core metadata and Swing companion
  generation.
- Multi-form dialog session helpers, including composite form models, Apply,
  Revert, Cancel, dirty-state helpers, action-state models, and active-editor
  focus support.
- Optimized table renderer helpers for high-volume business tables.

### Changed

- The annotation processor is split into focused form, table, action, option,
  dependency, and Swing-companion generation paths.
- Generated form bindings now install through presentation models so field
  values and visual component state can evolve separately.
- Dialog cancel/apply behavior now distinguishes original, current, and last
  applied baselines.

### Compatibility

- Deprecated `@SwingForm` and `@SwingTable` aliases remain available for Corusco
  1.0 source compatibility. They keep the 1.0 same-package Swing companion
  behavior while new code should migrate to `@CoruscoForm`, `@CoruscoTable`, and
  `@SwingCompanionPackage`.
- No intentional breaking changes from `1.0.0`.

## 1.0.0 - 2026-06-17

### Added

- `SortedList` and `MappedList` read-only observable collection views.
- Generated `<Form>Bindings` facades for installing generated behavior plans.
- Generated enum combo-box option metadata through `@ComboBox(enumOptions)`.
- Generated command factories and ordered descriptor/menu/toolbar metadata for
  `@UiAction` owners.
- `FormDialogShell` as a minimal native `JDialog` host for `FormDialog`.
- JApiCmp-based binary compatibility gate for runtime modules, with first-run
  `v1.0.0` baseline establishment under `.machine_env/api-baselines`.

### Changed

- Development version is now `1.0.0`.
- Local release readiness is exposed as `verifyReleaseReadiness`; the former
  `verifyPreviewReleaseReadiness` task remains as a compatibility alias.
- `corusco-test` is internal test support and is no longer part of the
  published artifact set.
- Preview package-surface review now includes the annotation subpackages:
  `annotations.command`, `annotations.form`, `annotations.help`,
  `annotations.table`, and `annotations.validation`.

### Breaking Changes

- Annotation imports are split by authoring area after `v0.1.0-preview`.
  Replace root-package annotation imports with the focused subpackages:
  form annotations from `cz.auderis.corusco.annotations.form`, table
  annotations from `.table`, validation constraints from `.validation`,
  command annotations from `.command`, and help metadata from `.help`.

## 0.1.0-preview - 2026-06-14

### Added

- Maven-local publication wiring for library modules with source and Javadoc
  jars.
- `verifyMavenLocalPublication` to verify local publication artifacts.
- Release policy covering preview semantic versioning and compatibility rules.
- JPMS module descriptors for published library modules.
- `verifyExamplesAgainstPublishedArtifacts` to compile examples from
  Maven-local Corusco artifacts.
- Preview API/package review gates for published packages and runtime
  reflection policy.
- `verifyGeneratedJavadocs` and `verifyPreviewReleaseReadiness` for local
  preview release checks.

### Changed

- Documentation now treats `corusco-glazedlists` as a first-class optional
  published module and clarifies that `corusco-examples` is an in-repository
  consumer rather than a published artifact.

### Breaking Changes

- None.

### Scope

- First preview artifact set for the roadmap minimum viable release.
- Typed runtime primitives, generated metadata/form/table support, Swing
  bindings, table state, dialogs, async validation, Glazed Lists interop,
  testing helpers, documentation, and examples.

### Release Gate

- All examples compile.
- Maven-local publication can be consumed by a local application.
- Generated Javadocs are readable.
- No core API depends on runtime reflection or a legacy reflection module.
