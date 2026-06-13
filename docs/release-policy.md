# Release Policy

Corusco is still in the preview line. The policy below explains what version
numbers mean before the first stable `1.0.0` release and what compatibility
breaks are allowed.

## Version Scheme

Published artifacts use semantic-version shaped coordinates:

```text
MAJOR.MINOR.PATCH[-QUALIFIER]
```

The current development version is `0.1.0-SNAPSHOT`. The first preview tag is
planned as `v0.1.0-preview`.

During the `0.x` line, each minor version represents a preview compatibility
line. A patch release should be compatible with the previous patch release in
the same minor line. A minor release may make breaking changes, but those
changes must be called out in the changelog.

## Compatibility Surface

The compatibility contract applies to published library modules:

- `corusco-core`
- `corusco-glazedlists`
- `corusco-swing`
- `corusco-annotations`
- `corusco-processor`
- `corusco-test`

`corusco-examples` is a source consumer and regression suite. It is not a
published compatibility surface.

The preview JPMS module names are:

| Artifact | JPMS module |
| --- | --- |
| `corusco-core` | `cz.auderis.corusco.core` |
| `corusco-glazedlists` | `cz.auderis.corusco.glazedlists` |
| `corusco-swing` | `cz.auderis.corusco.swing` |
| `corusco-annotations` | `cz.auderis.corusco.annotations` |
| `corusco-processor` | `cz.auderis.corusco.processor` |
| `corusco-test` | `cz.auderis.corusco.test` |

`corusco-glazedlists` currently publishes this name through
`Automatic-Module-Name` rather than `module-info.java` because Glazed Lists
1.11.0 does not provide a stable upstream JPMS module name.

Public runtime APIs are the public and protected types, constructors, methods,
fields, records, and sealed hierarchies in published artifacts. Annotation
types are public API because application source code depends on their names,
members, defaults, and retention/target contracts.

Generated source shape is also a compatibility surface when application code is
expected to call generated classes directly. Examples include generated field
key classes, descriptor classes, form models, table column classes, action
descriptor classes, view contracts, and behavior plans.

## Allowed Patch Changes

Patch releases in a preview minor line may:

- fix bugs without changing documented behavior;
- add overloads, new helper methods, or new generated constants;
- improve diagnostics and generated source readability;
- add optional metadata that existing source can ignore;
- improve documentation, examples, tests, or packaging metadata.

Patch releases must not remove or rename public API, change annotation member
defaults incompatibly, alter generated class names, or change generated member
types in a way that breaks recompilation of existing consumers.

## Breaking Changes

Breaking changes are allowed only in a new preview minor line or before the
first preview tag is cut. They must be recorded in `CHANGELOG.md` under
`Breaking Changes` with migration notes.

Examples of breaking changes:

- moving a public type to another package;
- renaming a published JPMS module;
- renaming typed keys, descriptor classes, generated form models, or generated
  table helpers;
- changing public method signatures or record component types;
- removing an annotation member or changing its default behavior;
- changing table persistence IDs generated from existing annotations;
- changing problem-code or resource-key IDs in a way that invalidates consumer
  resources or assertions.

## Binary Compatibility Checks

The project does not yet run an automated binary compatibility plugin. Until
that is added, each Stage 21 API-polish commit must review public API changes
manually and explain intentional breaks in the stage plan or changelog.

Before the first stable release, add an automated compatibility gate comparing
published artifacts against the previous preview release. The gate should check
binary compatibility for runtime modules and source compatibility for generated
API shapes where binary checking is not enough.

## Release Checklist

Before tagging a preview release:

1. Run the AudEnv-recommended test and build commands.
2. Run `publishToMavenLocal`, `verifyMavenLocalPublication`, and
   `verifyPublishedModuleMetadata`.
3. Run `verifyExamplesAgainstPublishedArtifacts` to confirm examples compile
   against Maven-local artifacts instead of project dependencies.
4. Run `verifyPreviewApiAudit` and review [Preview API Review](api-review.md)
   for public package boundaries and runtime reflection policy.
5. Update `CHANGELOG.md` with additions, fixes, breaking changes, and migration
   notes.
6. Confirm generated Javadocs are readable.
7. Create the release tag only after the working tree is clean.
