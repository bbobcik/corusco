# COR-124 Release 1.3.0

## Objective

Finalize the 1.3.0 version, release notes, compatibility documentation, and
GitHub tag for the typed data-plane release.

## Scope

- Change published project coordinates from `1.3.0-SNAPSHOT` to `1.3.0`.
- Freeze the 1.3.0 changelog and stable documentation references.
- Run the AudEnv-recommended tests, build, and `verifyReleaseReadiness` gate.
- Push the release commit and annotated `v1.3.0` tag to GitHub.

## Acceptance

- The release commit has a clean working tree and all local release gates pass.
- GitHub Actions passes for the release commit on `main`.
- `v1.3.0` resolves to the release commit on GitHub.
- The v1.2.0 compatibility baseline remains intact.

## Boundary

This stage releases the source and tag on GitHub. Maven Central publication is
not part of this stage unless credentials and a separately verified publication
acceptance path are available.
