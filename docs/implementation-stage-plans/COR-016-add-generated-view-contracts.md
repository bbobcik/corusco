# COR-016 Add Generated View Contracts

Commit message:

```text
COR-016 Add generated view contracts
```

## Roadmap Coverage

Roadmap Stage 10: Annotation Processor Phase 3, initial generated view contract
and behavior plan slice.

## Objective

Generate Swing view contracts and a simple behavior installation plan from
annotated form records.

## Dependencies

- Requires COR-009 behavior core.
- Requires COR-015 generated record form models.

## Scope

For each `@SwingForm` record, generate:

- `<RecordName>View`
- `<RecordName>BehaviorPlan`

Generated view methods:

- `JTextField` for `@TextField` and `@DateField`
- `JCheckBox` for `@CheckBox`
- `JComboBox<T>` for `@ComboBox`

Generated behavior plan:

- binds text/date fields to generated `TextFieldModel`s
- binds checkbox fields to generated `FieldModel<Boolean>`
- installs validation tooltip and border decorations for text fields
- installs select-all-on-focus behavior for text/date fields

## Required Deliverables

- Processor tests for generated source and behavior-plan compilation.
- Example showing generated view contract and behavior plan wiring a small
  headless Swing panel.
- Critical review against reflection policy and Swing/core module boundaries.

## Out of Scope

- Combo-box binding implementation.
- Help/static tooltip composition.
- Required/dirty marker visuals.
- Generated command/action behavior plans.

## Implementation Steps

1. Extend processor field specs with view method/component metadata.
2. Generate view interface source.
3. Generate behavior plan source using `BehaviorScope` and `StandardBehaviors`.
4. Add tests and example coverage.
5. Run `./gradlew clean build`.

## Acceptance Checks

- Generated view contract compiles.
- Missing view methods fail Java compilation because the view interface is not
  implemented.
- Generated behavior plan installs and disposes supported bindings repeatedly.
- Generated behavior plan contains no reflection.
