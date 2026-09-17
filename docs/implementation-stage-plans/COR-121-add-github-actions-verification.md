# COR-121 Add GitHub Actions verification

## Roadmap Coverage

GitHub-first continuous verification for the 1.3 development line.

## Outcome

Run JDK 25 Gradle tests and builds on pull requests and feature pushes, and run
the aggregate release-readiness gate on `main` pushes or manual dispatch.

## Acceptance Checks

- Workflow permissions are read-only for repository contents.
- Pull requests execute both `test` and `build`.
- Main/manual runs execute `verifyReleaseReadiness`.
- No publication credentials or machine-local paths are committed.
