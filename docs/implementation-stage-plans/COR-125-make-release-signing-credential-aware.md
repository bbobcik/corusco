# COR-125 Make release signing credential-aware

## Objective

Keep the stable release-readiness gate runnable in credential-free CI while
retaining signing for the explicit Sonatype publication path.

## Scope

- Configure publication signing only when `LOCAL_CREDENTIALS` is supplied.
- Preserve signing for the authenticated publication workflow.
- Rehearse the stable release gate from a Linux checkout with no signatory.

## Acceptance

- `verifyReleaseReadiness` passes on Windows and Linux without credentials.
- GitHub Actions passes for the release commit on `main`.
- The release tag identifies the corrected release commit.

## Boundary

This change does not publish to Maven Central and does not add or expose any
credentials. Authenticated publication remains a separate explicitly supplied
workflow.
