# COR-123 Make release verification portable

## Objective

Ensure the release-readiness gate invokes the Gradle wrapper that exists on the
current operating system, so the same verification works locally on Windows
and in GitHub Actions on Linux.

## Scope

- Select `gradlew.bat` on Windows and `gradlew` elsewhere.
- Keep the existing published-artifact compilation check and task ordering.
- Validate the full release-readiness task locally before pushing the repair.

## Acceptance

- `verifyReleaseReadiness` passes locally.
- The feature branch is pushed and its CI build passes.
- The merged `main` branch is pushed and both CI jobs pass, including release
  readiness.

## Stop conditions and rollback

Stop if the wrapper is missing, the release-readiness task fails for an
unrelated reason, or GitHub Actions cannot validate the pushed commit. The
repair is isolated to the root build script and can be reverted as a single
commit without changing public APIs or release metadata.
