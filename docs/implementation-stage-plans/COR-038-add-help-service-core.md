# COR-038 Add Help Service Core

Commit message:

```text
COR-038 Add help service core
```

## Roadmap Coverage

Roadmap Stage 15: Help, Tooltips, Resources, and Accessibility, core help
service slice.

## Objective

Introduce a Swing-free help service boundary for generated `HelpTopic`
descriptors. Runtime Swing behavior can later bind F1 or status UI to this
service without deciding in core whether help opens a dialog, browser, or
embedded viewer.

## Dependencies

- Requires existing `HelpTopic` keys.
- Builds on COR-037 typed resource lookup only conceptually; no direct runtime
  dependency is required.

## Scope

Add `corusco-core.help` APIs:

- `HelpRequest`
- `HelpHandler`
- `HelpService`
- `DefaultHelpService`

Required behavior:

- open help by stable topic;
- include optional source object and context text in requests;
- allow handler replacement/composition at application boundaries;
- expose the most recent request for tests and simple integrations;
- reject blank/null help topics through existing `HelpTopic` validation.

## Required Deliverables

- Public Javadocs documenting Swing-free dispatch, handler ownership, and
  request metadata.
- Unit tests for topic dispatch, source/context metadata, last-request
  tracking, missing handler behavior, and handler replacement.
- Example showing generated help topic dispatch with method-body comments.
- Stage-plan index update.

## Out of Scope

- F1 key binding.
- Swing `HelpBehavior`.
- Browser/dialog integration.
- Help content storage or localization.
- Tooltip composition.

## Implementation Steps

1. Add this stage plan and index entry.
2. Add core help package and request/handler/service types.
3. Add default service implementation.
4. Add tests for dispatch and lifecycle-independent behavior.
5. Add example using generated descriptor help topic.
6. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- Help requests carry topic, optional source, and optional context text.
- Opening help dispatches to the configured handler.
- Last request is retained for diagnostics/tests.
- Opening help without a handler fails with a clear exception.
- Help code remains Swing-free.
