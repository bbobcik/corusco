# COR-039 Add Swing Help Behavior

Commit message:

```text
COR-039 Add Swing help behavior
```

## Roadmap Coverage

Roadmap Stage 15: Help, Tooltips, Resources, and Accessibility, Swing
`HelpBehavior` and F1 dispatch slice.

## Objective

Connect generated `HelpTopic` descriptors to Swing components through the
behavior system. Components should be able to dispatch F1 to the core
`HelpService` while remaining lifecycle-owned and removable like existing
behaviors.

## Dependencies

- Requires COR-038 help service core.
- Requires existing Swing behavior scope infrastructure.

## Scope

Add Swing help behavior support:

- `BehaviorContext` exposes an optional `HelpService`;
- `BehaviorScope` can be created with a help service;
- standard `helpOnF1(HelpTopic)` behavior;
- F1 key binding installed through Swing input/action maps;
- cleanup removes installed key/action entries.

## Required Deliverables

- Public Javadocs documenting help-service ownership, F1 key binding, and
  lifecycle cleanup.
- Unit tests for F1 dispatch, source/context metadata, missing help-service
  failure, and cleanup.
- Example update showing generated help topic dispatch from a behavior with
  method-body comments.
- Stage-plan index update.

## Out of Scope

- Tooltip composition.
- Status-bar text behavior.
- Browser/dialog help handler implementations.
- Generated behavior-plan emission.

## Implementation Steps

1. Add this stage plan and index entry.
2. Extend behavior context/scope to carry an optional help service.
3. Add the standard F1 help behavior.
4. Add focused EDT tests.
5. Update behavior example to demonstrate generated help.
6. Run AudEnv compact test/build recommendations and review scans.

## Acceptance Checks

- F1 dispatches the configured help topic through `HelpService`.
- Help request source is the component and context identifies F1 dispatch.
- Installing help behavior without a help service fails clearly.
- Closing the behavior removes installed input/action map entries.
