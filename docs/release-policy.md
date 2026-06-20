# Release Policy

Corusco is in the stable `1.x` release line. The policy below explains the
published compatibility surface and the checks that must pass before a stable
release tag.

## Version Scheme

Published artifacts use semantic-version shaped coordinates:

```text
MAJOR.MINOR.PATCH[-QUALIFIER]
```

Snapshot versions are development coordinates and must not be treated as stable
consumer contracts. Stable releases use semantic versioning. Patch releases
preserve binary and source compatibility for the published API. Minor releases
may add API while preserving compatibility. Breaking changes require a new
major version and migration notes in the changelog.

## Compatibility Surface

The compatibility contract applies to published library modules:

- `corusco-core`
- `corusco-glazedlists`
- `corusco-swing`
- `corusco-annotations`
- `corusco-processor`

`corusco-test` and `corusco-examples` are in-repository test/example modules.
They are not published compatibility surfaces.

The JPMS module names are:

| Artifact | JPMS module |
| --- | --- |
| `corusco-core` | `cz.auderis.corusco.core` |
| `corusco-glazedlists` | `cz.auderis.corusco.glazedlists` |
| `corusco-swing` | `cz.auderis.corusco.swing` |
| `corusco-annotations` | `cz.auderis.corusco.annotations` |
| `corusco-processor` | `cz.auderis.corusco.processor` |

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

## Allowed Compatible Changes

Compatible releases may:

- fix bugs without changing documented behavior;
- add overloads, new helper methods, or new generated constants;
- improve diagnostics and generated source readability;
- add optional metadata that existing source can ignore;
- retain deprecated aliases for source compatibility across 1.x upgrades;
- improve documentation, examples, tests, or packaging metadata.

Compatible releases must not remove or rename public API, change annotation
member defaults incompatibly, alter generated class names, or change generated
member types in a way that breaks recompilation of existing consumers.

## Breaking Changes

Breaking changes after a stable release are allowed only in a new major line.
They must be recorded in `CHANGELOG.md` under `Breaking Changes` with migration
notes.

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

## Compatibility Checks

The project runs a JApiCmp-based binary compatibility gate for published
runtime modules. Generated source shape is reviewed through processor tests,
example compilation against Maven-local artifacts, Javadoc generation, and
manual API review because binary compatibility alone cannot describe generated
source contracts.

## Release Checklist

Before tagging a release:

1. Run the AudEnv-recommended test and build commands.
2. Run `verifyReleaseReadiness`.
3. Review [API Review](api-review.md) for public package boundaries and
   runtime reflection policy.
4. Update `CHANGELOG.md` with additions, fixes, breaking changes, and migration
   notes.
5. Create the release tag only after the working tree is clean.

`verifyReleaseReadiness` aggregates local publication, module metadata,
published-artifact example compilation, API/package audit, generated Javadoc
checks, and binary compatibility checks. It is not a substitute for reading
release notes and reviewing intentional API changes.
