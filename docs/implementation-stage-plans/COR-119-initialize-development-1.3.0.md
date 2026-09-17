# COR-119 Initialize development of version 1.3.0

## Roadmap Coverage

Post-1.2 development-line initialization.

## Outcome

Move the Gradle development version to `1.3.0-SNAPSHOT` while retaining the
`v1.2.0` binary compatibility baseline.

## Acceptance Checks

- All modules resolve `1.3.0-SNAPSHOT`.
- Binary compatibility continues to compare runtime modules with v1.2.0.
- No stable release tag is created by this development initialization.
