# COR-001 Bootstrap Repository Baseline

Commit message:

```text
COR-001 Bootstrap repository baseline
```

## Roadmap Coverage

Roadmap Stage 0: Repository and Engineering Bootstrap.

## Objective

Finish the repository baseline so future implementation commits start from a
known build, module, package, test, and documentation structure.

## Current Context

Already present:

- Gradle multi-project build rooted at `corusco`.
- Modules: `corusco-core`, `corusco-swing`, `corusco-annotations`,
  `corusco-processor`, `corusco-test`, and `corusco-examples`.
- JDK 25 toolchain configuration.
- JUnit and AssertJ dependencies through the version catalog.
- Basic inter-module dependencies for Swing, processor, test, and examples.

Still needed for this stage:

- Source directories and package roots.
- Baseline smoke tests.
- Initial README.
- ADR directory.
- Example smoke entry point.
- A verified `./gradlew clean build`.

## Scope

Create the project-level baseline only. Do not implement framework runtime
features in this commit.

Expected package roots:

- `cz.auderis.corusco.core`
- `cz.auderis.corusco.swing`
- `cz.auderis.corusco.annotations`
- `cz.auderis.corusco.processor`
- `cz.auderis.corusco.test`
- `cz.auderis.corusco.examples`

## Required Deliverables

- New code with Javadoc: package roots, package documentation where useful, and
  any smoke/example classes documented enough to establish project conventions.
- Tests: at least one JUnit 5 + AssertJ smoke test, plus enough build-level
  coverage to prove all modules participate in `clean build`.
- Examples: an initial simple Swing example in `corusco-examples` that future
  stages can revisit as framework APIs appear.

Expected documentation:

- `README.md` with project purpose, module map, build command, and JDK 25
  requirement.
- `docs/adr/README.md` explaining the ADR convention.

Expected smoke coverage:

- A trivial `corusco-core` JUnit 5 test using AssertJ.
- A trivial `corusco-examples` class that can construct a Swing window on the
  EDT without showing it during automated tests.

## Out of Scope

- Runtime APIs such as subscriptions, values, keys, or problems.
- Annotation processor implementation.
- Checkstyle, SpotBugs, Error Prone, publishing, and CI service configuration.
- Any generated code.

## Implementation Steps

1. Add source and test directory roots for modules that need smoke coverage.
2. Add package-level documentation where useful, especially in `corusco-core`.
3. Add project README with concise architecture positioning from the roadmap.
4. Add ADR directory and a README describing filename format.
5. Add a core build smoke test that proves JUnit and AssertJ are wired.
6. Add an example smoke class with EDT-safe Swing window construction.
7. Add Javadoc/package documentation for public smoke classes and package roots.
8. Run `./gradlew clean build`.

## Acceptance Checks

- All subprojects compile.
- JUnit 5 discovers and runs at least one test.
- AssertJ is usable in test code.
- `corusco-processor` remains present and compiles even if it has no processor
  yet.
- The example module compiles and contains a minimal Swing smoke class.
- Public smoke/example code has Javadoc where it establishes a convention.
- `./gradlew clean build` passes on JDK 25.

## Review Focus

- Package names match the repository's `cz.auderis.corusco` group.
- No framework behavior leaks into the bootstrap commit.
- Documentation does not promise implemented APIs that do not exist yet.
