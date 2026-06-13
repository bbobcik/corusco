# Corusco Testing Guide

Corusco tests should exercise typed presentation contracts rather than private
Swing fields, JavaBeans names, runtime annotations, or generated source layout
details. The current test support is split into two main paths:

- runtime and Swing tests use ordinary JUnit tests plus EDT-aware helpers;
- annotation-processor tests use `GeneratedSourceCompiler` to compile sample
  sources and inspect generated Java.

Use the smallest level that proves the behavior under test. Form models,
validation rules, table state merge, command state, async generation counters,
and dialog results can usually be tested without opening Swing windows.
Presenter/view wiring should use generated keys and `SwingMvpTester`.

## Test Layers

| Layer | Typical target | Main tools |
| --- | --- | --- |
| Core model tests | Values, lists, forms, validation, commands, resources, table state, tasks, dialogs | JUnit, AssertJ, direct model APIs |
| Swing adapter tests | Bindings, behaviors, table models, dialog bindings, task callbacks | EDT-aware tests, `SwingEdt.runAndWait(...)` |
| Presenter/view tests | Generated component contracts and presenter behavior | `SwingMvpTester`, `SwingComponentKeys`, generated keys |
| Processor tests | Generated source shape and diagnostics | `GeneratedSourceCompiler`, `GeneratedSourceCompilation` |
| Example regression tests | Public usage scenarios | Example `runScenario()` methods with concise assertions |

Keep tests focused. A table-state merge test should not create a native window,
and a processor test should not reimplement the runtime behavior that generated
code will call later.

## Swing MVP Tests

`SwingMvpTester<V, P>` creates the view and optional presenter on the EDT, then
runs lookup, input, command, behavior, problem, and table helpers on the EDT.
Views mark components with generated or handwritten `ComponentKey` constants:

```java
private static final ComponentKey<JTextField> NAME_FIELD =
        ComponentKey.of("customer/name-field", JTextField.class);

private final JTextField nameField = SwingComponentKeys.mark(new JTextField(), NAME_FIELD);
```

Create a tester from view and presenter factories:

```java
SwingMvpTester<CustomerView, CustomerPresenter> tester = SwingMvpTester.create(
        CustomerView::new,
        CustomerPresenter::new,
        (view, presenter) -> presenter.commands()
);
```

Prefer tester helpers over direct Swing field access:

```java
tester.enterText(NAME_FIELD, "Alice")
        .setSelected(ACTIVE_BOX, true)
        .selectItem(TYPE_COMBO, CustomerType.VIP)
        .assertCommandEnabled(SAVE, true)
        .executeCommand(SAVE);
```

When a test needs a custom assertion, use `runOnEdt(...)` or
`queryOnEdt(...)`:

```java
String text = tester.queryOnEdt((view, presenter) -> view.nameField.getText());
```

Do not mutate Swing state from the test thread after fetching a component with
`requireComponent(...)`; the lookup is EDT-safe, but the returned component is
still Swing-owned.

## Components and Commands

Component lookup uses `ComponentKey<C>`, not component names or reflection.
Duplicate matching components are rejected so generated view contracts do not
silently bind tests to the wrong widget.

Command assertions use `ActionKey` and the presenter-supplied `CommandSet`:

```java
tester.assertCommandEnabled(CustomerActions.SAVE_KEY, true)
        .executeCommand(CustomerActions.SAVE_KEY)
        .assertCommandSelected(CustomerActions.ACTIVE_KEY, false);
```

This exercises the same command model used by buttons, menu items, and key
bindings without depending on Swing action-map details.

## Problems and Behaviors

Problem assertions read a `ProblemSet` on the EDT and match through typed
filters:

```java
tester.assertProblem(
        (view, presenter) -> presenter.form().problems(),
        CustomerFields.NAME.asFieldKey(),
        CustomerProblems.NAME_REQUIRED
);
```

Behavior assertions use stable `BehaviorKey` values:

```java
tester.assertBehaviorInstalled(
        NAME_COMPONENT,
        (view, presenter) -> presenter.behaviors(),
        StandardBehaviorKeys.SELECT_ALL_ON_FOCUS
);
```

Assert behavior keys and problem targets, not private listener classes, border
implementations, or generated field names.

## Table Tests

Use view-row helpers when a test describes the visible table order, and
model-row helpers when a test describes the row source:

```java
tester.selectTableViewRow(CUSTOMER_TABLE, 0)
        .assertSelectedTableModelRow(CUSTOMER_TABLE, 2);

tester.selectTableModelRow(CUSTOMER_TABLE, 1)
        .assertSelectedTableModelRow(CUSTOMER_TABLE, 1);
```

For persisted state, assert stable table and column ids:

```java
tester.assertTableStateId((view, presenter) -> presenter.tableState(), "customer/search")
        .assertTableColumnVisible((view, presenter) -> presenter.tableState(), "customer/search/name", true)
        .assertTableSort((view, presenter) -> presenter.tableState(),
                "customer/search/name",
                SortDirection.ASCENDING,
                0);
```

Avoid assertions against `TableColumn` object identity or transient view-column
indexes when the behavior under test is table-state persistence.

## Generated-Source Tests

`GeneratedSourceCompiler` compiles sample sources under a caller-owned
temporary directory, applies supplied annotation processors, and writes
generated files to a per-compilation output directory:

```java
GeneratedSourceCompilation result = GeneratedSourceCompiler.in(tempDir)
        .withProcessor(new CoruscoAnnotationProcessor())
        .compile("demo/CustomerEdit.java", source);

assertThat(result.success()).isTrue();
result.assertGeneratedSourceContains(
        "demo/CustomerEditFields.java",
        "public final class CustomerEditFields",
        "FieldKey.of(\"customer/name\""
);
```

Use diagnostics for negative tests:

```java
assertThat(result.success()).isFalse();
assertThat(result.messages()).contains("@UiAction methods must not declare parameters");
```

`generatedSource(...)` normalizes line endings. Prefer checking meaningful
snippets and generated contracts instead of asserting a full file unless the
whole generated source shape is the behavior being reviewed.

See [Generated Code Examples](generated-code-examples.md) for the current
annotated examples and generated companion classes that processor tests should
keep readable.

## Example Regression Tests

Examples expose `runScenario()` methods and tests assert their returned
diagnostics. Keep examples short but realistic:

- method bodies should include comments where ownership, cleanup, threading, or
  generated-code conventions are not obvious;
- examples should compile against current public APIs;
- tests should assert behavior, not merely that the example returns something;
- examples should stay close to the guide text for the same feature.

This keeps examples useful as both documentation and regression fixtures.

## Build and Test Commands

Before running test or build commands, ask AudEnv for the current project
recommendation:

```powershell
audenv recommend test --project .
audenv recommend build --project .
```

Use the `project-compact:` command when AudEnv provides one. It keeps Gradle
output short in the conversation and writes full logs under `.machine_env/logs`.
For this project, the underlying commands are:

```powershell
.\gradlew.bat test --quiet --stacktrace
.\gradlew.bat build --quiet --stacktrace
```

Run `git diff --check` before staging and `git diff --cached --check` before
committing.

## Current Limits

- `SwingMvpTester` is a headless Swing harness; it does not drive native window
  managers or real modal dialogs.
- Component-key lookup requires components to be marked explicitly with
  `SwingComponentKeys.mark(...)`.
- Generated-source helpers compile javac samples; they do not execute generated
  runtime behavior unless a separate test instantiates that code.
- There is no screenshot, accessibility tree, or visual regression test helper
  yet.
- There is no automated binary compatibility checker yet. Until one is added,
  preview API changes are reviewed against [Release Policy](release-policy.md).
