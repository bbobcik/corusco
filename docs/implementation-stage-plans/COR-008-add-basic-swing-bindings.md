# COR-008 Add Basic Swing Bindings

Commit message:

```text
COR-008 Add basic Swing bindings
```

## Roadmap Coverage

Roadmap Stage 5: Basic Swing Bindings.

## Objective

Connect the non-Swing form/value/problem primitives to Swing components through
small disposable bindings with explicit EDT discipline.

## Dependencies

- Requires COR-002 lifecycle subscriptions.
- Requires COR-003 observable values.
- Requires COR-005 problem model.
- Requires COR-006 field model core.
- Requires COR-007 validation rule core for validation problem examples.

## Scope

Add Swing binding APIs in `corusco-swing`, under packages such as:

- `cz.auderis.corusco.swing.binding`

Candidate public API:

- `Binding`
- `BindingScope`
- `BindingFactory`
- `SwingEdt`
- `SwingEditors`

Expected initial bindings:

- `JTextField` to `TextFieldModel`
- `JTextArea` to `TextFieldModel`
- `JCheckBox` or `AbstractButton` selected state to `FieldModel<Boolean>`
- `JLabel` text to `ReadableValue<String>`
- `AbstractButton` enabled state to `ReadableValue<Boolean>`
- validation tooltip binding from `ReadableValue<ProblemSet>`
- validation border binding from `ReadableValue<ProblemSet>`

## Required Deliverables

- New code with Javadoc: binding APIs must document ownership, disposal,
  feedback-loop prevention, EDT requirements, and validation rendering limits.
- Tests: cover text edits updating raw text, semantic value updates after valid
  parse, invalid parse problem state, model-to-component updates, selected and
  enabled bindings, disposal removing listeners, and validation tooltip/border
  updates.
- Examples: add a simple Swing binding example that constructs and exercises
  bound components on the EDT without showing a native window.

## Out of Scope

- Behavior abstraction.
- Command/action model.
- Rich validation rendering policy.
- Async/debounced validation.
- Native window lifecycle tests.

## Implementation Steps

1. Define `Binding` and `BindingScope`.
2. Add an EDT helper used by bindings and tests.
3. Implement text bindings with a feedback-loop guard.
4. Implement checkbox, label, and enabled-state bindings.
5. Expose observable problem state from text field models where needed.
6. Implement simple validation tooltip and border bindings.
7. Add a headless-safe Swing binding example.
8. Add focused EDT tests.
9. Run `./gradlew clean build`.

## Acceptance Checks

- Editing a `JTextField` updates `TextFieldModel.rawText`.
- Valid parse updates semantic value.
- Invalid parse creates problem state.
- Closing a binding removes Swing and model listeners.
- Model-originated changes update bound components without feedback loops.
- Validation tooltip and border bindings reflect problem state.
- Public binding APIs have Javadoc.
- No Swing dependency is introduced into `corusco-core`.

## Review Focus

- Bindings must be easy to replace with behavior-based bindings in later stages.
- EDT failures should be explicit and early.
- The implementation should avoid hiding Swing mechanics from advanced Swing
  users.
