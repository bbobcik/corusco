# Changelog

All notable changes to Corusco are recorded here. The project follows the
preview compatibility rules in [docs/release-policy.md](docs/release-policy.md).

## Unreleased

### Added

- Maven-local publication wiring for library modules with source and Javadoc
  jars.
- `verifyMavenLocalPublication` to verify local publication artifacts.
- Release policy covering preview semantic versioning and compatibility rules.
- JPMS module descriptors for published library modules.
- `verifyExamplesAgainstPublishedArtifacts` to compile examples from
  Maven-local Corusco artifacts.

### Changed

- Documentation now treats `corusco-glazedlists` as a first-class optional
  published module and clarifies that `corusco-examples` is an in-repository
  consumer rather than a published artifact.

### Breaking Changes

- None since the preview release line has not been tagged yet.

## 0.1.0-preview - Planned

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
