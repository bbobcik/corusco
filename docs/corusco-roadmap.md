# Auderis Corusco — Development Roadmap

> Working title: **Auderis Corusco**  
> Target platform: **JDK 25**, Swing/AWT, Gradle Groovy DSL, JUnit 5, AssertJ  
> Primary architectural style: **MVP + Presentation Model + generated metamodel + attachable behaviors**

---

## 1. Executive Summary

The project will produce a modern, high-performance Swing presentation framework for business GUI applications. Its purpose is to replace ad-hoc Swing wiring, obsolete JavaBeans-style binding, stringly-typed property paths, and scattered UI metadata with a coherent set of compile-time generated, strongly typed presentation models, bindings, behaviors, actions, validation facilities, table models, and persistent UI-state helpers.

The design borrows successful concepts from:

- **JGoodies Binding**: presentation model discipline, buffered values, form-centric thinking.
- **GlazedLists**: observable collections, evented transformations, Swing table/list adapters, precise list changes.
- **BSAF / Swing Application Framework**: actions, lifecycle, resources, task concepts, session state.
- **SwingX**: highlighters, painters, prompt/help/feedback UX ideas.
- **Apache Wicket**: component trees, models, detachable models, feedback filtering, and especially **Behavior** as an attachable component plug-in.

The framework shall not replace Swing. It shall make Swing application construction more explicit, testable, type-safe, and mechanically reliable.

---

## 2. Project Goals

### 2.1 Primary Goals

1. Provide a **typed presentation model layer** for Swing applications.
2. Support **transactional form editing**, especially modal dialog forms with OK/Cancel/Apply semantics.
3. Eliminate or minimize runtime reflection by using **javac annotation processing**.
4. Eliminate arbitrary string constants in public contracts by generating typed keys and descriptors.
5. Provide high-performance, precise **observable values**, **observable lists**, and **table models**.
6. Provide reusable, lifecycle-aware **behaviors** for component enrichment.
7. Provide a first-class **command/action model** that adapts cleanly to Swing `Action`.
8. Provide coherent **validation and feedback** facilities.
9. Provide support for **context help, tooltips, status text, accessibility descriptions**, and generated resource keys.
10. Provide table-column metadata and **persistent table column state**: width, order, visibility, sort state, and migration.
11. Provide deterministic lifecycle/disposal to avoid classic Swing listener leaks.
12. Provide a test harness suitable for presenter, model, binding, behavior, and generated-code tests.
13. Ultimately produce a single library artifact for client applications to use.

### 2.2 Secondary Goals

1. Support lazy/detachable data models for expensive business data.
2. Support asynchronous tasks using Java virtual threads, while keeping Swing-bound state EDT-confined.
3. Support generated view contracts and generated binding plans.
4. Support optional resource/session-state modules.
5. Support JPMS modules, but avoid making JPMS mandatory for simple applications.
6. Ensure that Javadoc documentation is extensive, complete and up-to-date, and follows best documentation practices.

---

## 3. Non-Goals

The project shall not become:

1. A visual GUI builder.
2. A replacement for Swing/AWT.
3. A dependency-injection framework.
4. A persistence framework.
5. A docking/windowing platform.
6. A clone of NetBeans Platform, Eclipse RCP, or Wicket.
7. A general JavaBeans reflection binding framework.
8. A framework that requires runtime classpath scanning.
9. A framework that serializes Swing component trees.
10. A framework that expresses business rules as strings or expression-language fragments.
11. A framework that hides Swing so completely that advanced Swing developers cannot reason about it.

---

## 4. Foundation Principles

### 4.1 Swing Remains Swing

The framework must not fight Swing. It should use Swing’s native concepts:

- `JComponent`
- `Action`
- `TableModel`
- `ListModel`
- `ComboBoxModel`
- `ButtonModel`
- `Document`
- `InputMap` / `ActionMap`
- `JLayer`
- `JTableHeader`
- `JRootPane`
- `JDialog`
- `JFrame`

The framework provides structured plumbing around these concepts, not a parallel UI universe.

### 4.2 Compile-Time Specialization Over Runtime Discovery

The annotation processor generates ordinary Java code:

- direct record accessor calls
- direct constructor calls
- direct method references
- direct binding setup
- direct command creation
- direct table-column definitions
- direct validation rule installation

The runtime framework must not need to discover annotated fields or methods reflectively.

### 4.3 Type-Safe Keys Instead of String Contracts

Public APIs should use typed keys:

```java
FieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT;
ColumnKey<CustomerRow, String> NAME;
ActionKey SAVE;
ComponentKey<JTextField> NAME_FIELD;
ResourceKey<String> NAME_TOOLTIP;
HelpTopic CUSTOMER_NAME;
```

Strings may exist only at system boundaries:

- generated metadata internals
- resource bundle keys
- serialized preferences
- diagnostics
- optional legacy/reflection module

Application code should not write property paths such as:

```java
"customer.address.city"
```

Generated key ids should use stable identity tokens that do not masquerade as
Java bean paths. For example, prefer generated constants backed by ids such as
`customer/name` or `customer/credit-limit` over hand-written property paths.

### 4.4 EDT Confinement by Default

All Swing-observed mutable state is EDT-confined unless explicitly documented otherwise.

Background work may use virtual threads or executors, but all mutation of Swing-bound models must return to the EDT through framework services.

### 4.5 Transactional Form Editing

Business forms edit transaction buffers, not persistent domain objects.

Recommended flow:

```text
domain object
    -> immutable edit DTO / record
        -> generated form model
            -> user editing state
        -> validation
    -> dialog result / accepted edit DTO
        -> application service persists/applies change
```

### 4.6 Invalid Intermediate Input Is Valid UI State

A text field must be able to hold unparseable or incomplete user input.

Example:

```text
raw text: "12,"
semantic value: unavailable
parse state: intermediate or failed
```

This is essential for numeric, date, time, and formatted fields.

### 4.7 Behavior as First-Class Component Extension

Reusable component enrichment should be implemented as attachable behaviors, inspired by Wicket’s `Behavior` concept.

Examples:

- text binding behavior
- validation border behavior
- tooltip/help behavior
- dirty marker behavior
- table column state behavior
- command button behavior
- key binding behavior
- select-all-on-focus behavior
- busy overlay behavior
- diagnostics behavior

### 4.8 Small, Explicit Runtime Core

The runtime should provide small primitives that compose well. Avoid a giant application framework.

The library should be understandable by an advanced Swing developer by reading the public API and generated code.

---

## 5. Design Rules, Patterns, and Constraints

### 5.1 Runtime Reflection Policy

#### Allowed

- `Class<T>` type tokens.
- `Class<?>` for `JTable.getColumnClass()`.
- Resource-key strings at localization boundary.
- Serialized IDs in table/session state.
- Optional debug metadata.

#### Forbidden in Core Runtime

- `Introspector`
- `PropertyDescriptor`
- `Method.invoke()`
- `Field.get()` / `Field.set()`
- `setAccessible(true)`
- runtime annotation scanning
- runtime classpath scanning
- string property paths

#### Optional Escape Hatch

A future `corusco-reflect` module may provide legacy adapters, but it must not be a dependency of the core modules.

### 5.2 Annotation Processing Constraints

The processor shall:

- use `javax.lang.model.*`, not reflection;
- generate deterministic, readable Java source;
- keep source-generation templates modular and readable; prefer a real template
  engine such as Velocity when generation becomes broad enough to justify the
  dependency, otherwise use Java text blocks rather than long sequences of
  string writes;
- report errors with exact source positions where possible;
- favor isolating processor behavior for Gradle incremental compilation;
- avoid global registries in early stages;
- avoid parsing method bodies;
- avoid executing user code;
- avoid requiring preview language features.

### 5.3 Generated Code Rules

Generated code should be:

- simple;
- verbose enough to debug;
- deterministic;
- readable in IDEs;
- final where appropriate;
- free of clever dynamic dispatch;
- compatible with JDK 25 stable features;
- covered by golden-source and behavioral tests.

Generated code should prefer:

```java
this.name = fields().text(CustomerEditFields.NAME, original.name(), Converters.string());
```

over dynamic loops such as:

```java
for (FieldDescriptor<?, ?> descriptor : descriptors) {
    createFieldDynamically(descriptor);
}
```

### 5.4 Form Model Rules

A generated form model shall:

- own field models;
- track dirty state;
- track touched state;
- track parse state;
- track validation problems;
- expose committability;
- provide reset;
- provide `toResult()`;
- avoid mutating the original domain object;
- support form-level validators;
- support async validation later.

### 5.5 Validation Rules

Validation is divided into:

1. parsing/conversion problems;
2. field validation problems;
3. cross-field validation problems;
4. table row/cell problems;
5. async/server-side problems;
6. warnings and informational messages.

Annotations may describe simple local constraints:

```java
@Required
@Length(max = 80)
@DecimalRange(min = "0.00")
```

Complex validation shall use typed Java DSL:

```java
rules.on(CustomerEditFields.VALID_FROM, CustomerEditFields.VALID_TO)
     .check(this::validDateRange);
```

No expression-language rules in the MVR.

### 5.6 Behavior Rules

A behavior:

- may observe model state;
- may decorate a component;
- may install listeners/input maps/action maps;
- may maintain private auxiliary state;
- may trigger commands;
- must have deterministic disposal;
- must not secretly replace the primary value model;
- must declare ordering/phase if order matters;
- must support conflict detection where feasible.

### 5.7 Command/Action Rules

Commands are first-class presentation objects. A command owns:

- action key;
- text;
- tooltip;
- icon;
- accelerator;
- mnemonic;
- enabled state;
- selected state for toggle/radio actions;
- handler.

Swing buttons, menu items, toolbar buttons, popup menu items, and key bindings must share the same command instance.

### 5.8 Table Model Rules

Table metadata must be stable and generated.

A table column must have:

- stable column key;
- stable persistence ID;
- header resource;
- optional tooltip/help metadata;
- value type;
- getter;
- optional updater/wither;
- default width/order/visibility;
- capabilities: sortable, filterable, editable, hideable.

Table adapters must use precise events whenever possible. `fireTableDataChanged()` is a fallback, not the normal path.

### 5.9 Persistent State Rules

Persistent UI state shall be explicit data, not serialized components.

Persistable state includes:

- table column widths;
- table column order;
- table column visibility;
- table sort order;
- split pane divider positions;
- window bounds;
- selected tab;
- recent filter/search text.

Serialized state may use string IDs, but application code must use typed keys.

### 5.10 Testing Rules

Every stage must include:

- unit tests for runtime primitives;
- annotation processor tests;
- generated-source assertions;
- behavioral tests using JUnit 5 and AssertJ;
- EDT discipline tests where applicable;
- leak/disposal tests for bindings and behaviors;
- table event precision tests.

---

## 6. Proposed Module Structure

Initial multi-project Gradle layout:

```text
corusco/
  settings.gradle
  build.gradle
  gradle.properties

  corusco-core/
  corusco-swing/
  corusco-annotations/
  corusco-processor/
  corusco-test/
  corusco-examples/
```

### 6.1 `corusco-core`

Runtime without Swing dependency where possible.

Responsibilities:

- observable values;
- subscriptions;
- lifecycle/disposal;
- form model abstractions;
- validation/problem model;
- command model;
- observable collections;
- typed keys;
- detachable values;
- task abstractions, if not split later.

### 6.2 `corusco-swing`

Swing/AWT integration.

Responsibilities:

- Swing component bindings;
- behavior implementations;
- `Action` adapters;
- table/list/combo adapters;
- dialog helpers;
- `JLayer` overlays;
- table-state controller;
- help/tooltip/status behaviors;
- validation decoration.

Requires `java.desktop`.

### 6.3 `corusco-annotations`

Compile-time annotation API.

Responsibilities:

- `@CoruscoForm`
- `@TextField`
- `@CheckBox`
- `@ComboBox`
- `@CoruscoTable`
- `@Column`
- `@UiAction`
- `@Help`
- `@Required`
- `@Length`
- `@DecimalRange`
- marker interfaces for typed keys

Annotations should have minimal runtime retention, preferably `SOURCE` or `CLASS` depending on needs.

### 6.4 `corusco-processor`

Annotation processor.

Responsibilities:

- parse annotated declarations through `javax.lang.model`;
- validate annotation usage;
- generate field keys;
- generate form models;
- generate descriptors;
- generate table columns;
- generate action descriptors;
- generate view contracts and behavior plans where enabled;
- provide helpful compile-time errors.

### 6.5 `corusco-test`

Testing utilities.

Responsibilities:

- EDT test harness;
- presenter/view tester;
- fake resource service;
- fake help service;
- fake table state store;
- annotation processor test helpers;
- generated source normalization utilities.

### 6.6 `corusco-examples`

Runnable examples and regression playground.

Examples:

- simple customer edit dialog;
- master-detail customer search;
- invoice table with editable lines;
- help/tooltip demo;
- table-state persistence demo;
- async validation demo.

---

## 7. Initial Gradle Baseline

Use Gradle Groovy DSL.

Root `build.gradle` sketch:

```groovy
plugins {
    id 'java-library' apply false
}

subprojects {
    apply plugin: 'java-library'

    group = 'cz.auderis.corusco'
    version = '0.1.0-SNAPSHOT'

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    tasks.withType(JavaCompile).configureEach {
        options.encoding = 'UTF-8'
        options.compilerArgs += [
            '-Xlint:all',
            '-Werror'
        ]
    }

    test {
        useJUnitPlatform()
    }

    dependencies {
        testImplementation platform('org.junit:junit-bom:6.0.0')
        testImplementation 'org.junit.jupiter:junit-jupiter'
        testImplementation 'org.assertj:assertj-core:3.27.7'
    }
}
```

Version numbers should be pinned in a version catalog or dependency constraints once the project stabilizes. The exact JUnit/AssertJ versions may be adjusted during bootstrap.

Processor module descriptor for Gradle incremental AP:

```text
corusco-processor/src/main/resources/META-INF/gradle/incremental.annotation.processors
```

Initial content:

```text
cz.auderis.corusco.processor.CoruscoProcessor,isolating
```

If later global registries are generated, split that into a separate aggregating processor or make it opt-in.

---

## 8. Core Domain Vocabulary

### 8.1 Typed Keys

```java
FieldKey<O, T>
TextFieldKey<O, T>
ColumnKey<R, V>
TableKey<R>
ActionKey
ComponentKey<C>
ResourceKey<T>
HelpTopic
ProblemCode
BehaviorKey
MetaKey<T>
```

`ComponentKey<C>` is intentionally generic in core. Swing-specific type bounds,
such as `ComponentKey<JTextField>`, belong in Swing integration code and should
not make `corusco-core` depend on `java.desktop`.

### 8.2 Lifecycle

```java
Disposable
Subscription
SubscriptionScope
BindingScope
BehaviorScope
Presenter
View<P extends Presenter>
```

### 8.3 Values

```java
ReadableValue<T>
WritableValue<T>
DerivedValue<T>
BufferedValue<T>
TextValue<T>
LoadableValue<T>
DetachableValue<T>
```

### 8.4 Forms

```java
FieldModel<T>
TextFieldModel<T>
FormModel<R>
FormRules<M>
RuleSet<M>
Problem
ProblemSet
ProblemSource
ProblemFilter
```

### 8.5 Collections and Tables

```java
ObservableList<E>
ListChange<E>
ListChangeSet<E>
ObservableTableModel<R>
Column<R, V>
ColumnDescriptor<R, V>
TableState
TableStateStore
```

### 8.6 Commands and Actions

```java
Command
CommandSet
CommandFactory
ActionDescriptor
SwingActionAdapter
```

### 8.7 Behaviors

```java
ViewBehavior<C extends JComponent>
BindingBehavior<C extends JComponent>
DecorationBehavior<C extends JComponent>
BehaviorDescriptor
BehaviorPhase
BehaviorContext
BehaviorFactory
BehaviorPlan
```

---

## 9. Staged Implementation Plan

The stages below are designed so that each stage produces usable, testable capability and avoids premature framework expansion.

---

# Stage 0 — Repository and Engineering Bootstrap

## Objective

Create the project skeleton, coding conventions, test infrastructure, and baseline CI-ready Gradle build.

## Deliverables

1. Multi-project Gradle build.
2. JDK 25 toolchain configuration.
3. JUnit 5 + AssertJ test baseline.
4. Package naming convention.
5. Basic Checkstyle/Error Prone/SpotBugs decision deferred, but compiler warnings enabled.
6. Initial README.
7. Architecture Decision Record directory.
8. Simple CI task:

```bash
./gradlew clean build
```

## Acceptance Criteria

- All subprojects compile.
- A trivial JUnit 5 test runs.
- AssertJ assertions are available.
- The annotation processor module is present but may be empty.
- The examples module can run a trivial Swing window.

---

# Stage 1 — Core Lifecycle, Values, and Subscriptions

## Objective

Build the small runtime foundation that every later feature uses.

## Features

1. `Subscription`
2. `SubscriptionScope`
3. `Disposable`
4. `ReadableValue<T>`
5. `WritableValue<T>`
6. `SimpleValue<T>`
7. `DerivedValue<T>`
8. `MappedValue<A, B>`
9. `ChangeOrigin`
10. `ValueChangeEvent<T>` as an immutable record event type
11. EDT assertion utilities in `corusco-swing`

## Design Notes

- Value mutation shall be synchronous.
- Listener iteration must be robust against listener removal during dispatch.
- Avoid allocating excessive objects in hot paths where simple direct dispatch is possible.
- Explicitly document threading assumptions.

## Acceptance Criteria

- Value changes notify subscribers exactly once.
- No notification when value remains equal, unless explicit invalidation is requested.
- Subscriptions can be closed idempotently.
- Derived values update when dependencies update.
- Tests cover listener removal during event dispatch.

---

# Stage 2 — Typed Keys and Problem Model

## Objective

Introduce typed identity for fields, resources, actions, components, problems, and validation targets.

## Features

1. `FieldKey<O, T>`
2. `TextFieldKey<O, T>`
3. `ResourceKey<T>`
4. `HelpTopic`
5. `ActionKey`
6. `ProblemCode`
7. `ProblemSeverity`
8. `ProblemTarget` sealed hierarchy
9. `Problem`
10. `ProblemSet`
11. `ProblemSource`
12. `ProblemFilter`

## Problem Target Types

```java
ProblemTarget.Form
ProblemTarget.Field<O, T>
ProblemTarget.Row<R>
ProblemTarget.Cell<R, V>
ProblemTarget.Component<C>
```

## Acceptance Criteria

- Problem filtering works by field, form, severity, row, and cell.
- Problem targets are strongly typed.
- No public API accepts arbitrary field-name strings for problem targeting.
- Pattern switch over problem targets is clean and exhaustive where possible.

---

# Stage 3 — Field Models, Text Models, Conversion, and Form Model Core

## Objective

Build a handwritten form model infrastructure before generating any code.

## Features

1. `FieldModel<T>`
2. `TextFieldModel<T>`
3. `ParseState<T>` sealed hierarchy
4. `StringConverter<T>`
5. `ParseResult<T>` sealed hierarchy
6. standard converters:
   - `String`
   - `Integer`
   - `Long`
   - `BigDecimal`
   - `LocalDate`
   - `Enum`
7. `FormModel<R>`
8. `AbstractFormModel<R>`
9. dirty state
10. touched state
11. reset
12. accept current values as original
13. committability
14. `toResult()` contract

## Acceptance Criteria

- Text fields can hold unparseable raw text without destroying previous semantic value.
- Empty/null policy is configurable per field/converter.
- Dirty state changes when value differs from original.
- Reset restores original state.
- `toResult()` refuses invalid/uncommittable forms.
- All state mutation is testable without Swing.

---

# Stage 4 — Validation and Feedback Core

## Objective

Implement validation pipeline and problem aggregation.

## Features

1. `FieldValidator<T>`
2. `FormValidator<M>`
3. `RuleSet<M>` typed dependency DSL
4. simple generated-compatible constraint implementations:
   - required
   - length
   - decimal min/max
   - integer min/max
   - regex
   - date past/future/present variants
5. validation timing metadata:
   - on change
   - on commit
   - debounced later
6. problem aggregation from fields and form rules
7. problem filters for summaries/tooltips/decorators

## Acceptance Criteria

- Field validation runs after parse success.
- Parse failure becomes a problem.
- Cross-field validation can depend on typed field keys.
- Changing one field only revalidates relevant rules where dependency information is available.
- Problem summaries can filter by severity and target.

---

# Stage 5 — Basic Swing Bindings

## Objective

Connect the form model core to actual Swing components.

## Features

1. `Binding`
2. `BindingScope`
3. `BindingFactory`
4. `JTextField` binding
5. `JTextArea` binding
6. `JCheckBox` binding
7. `JLabel` read-only value binding
8. `JButton` command/action binding placeholder
9. validation tooltip binding
10. validation border binding
11. dirty marker placeholder
12. commit-active-editor utility for dialogs

## Design Notes

- Bindings must be disposable.
- Bindings must avoid feedback loops.
- Text binding should support raw text and parse state.
- Swing mutation must happen on EDT.

## Acceptance Criteria

- Editing a `JTextField` updates `TextFieldModel.rawText`.
- Valid parse updates semantic value.
- Invalid parse creates problem state.
- Closing a binding removes listeners.
- Repeated open/close of test dialog does not accumulate listeners.

---

# Stage 6 — Behavior Core

## Objective

Introduce Wicket-inspired behavior mechanism as the unifying component-extension abstraction.

## Features

1. `ViewBehavior<C extends JComponent>`
2. `BindingBehavior<C extends JComponent>`
3. `DecorationBehavior<C extends JComponent>`
4. `BehaviorScope`
5. `BehaviorContext`
6. `BehaviorFactory`
7. `BehaviorPhase`
8. `BehaviorKey`
9. conflict/cardinality metadata
10. behavior installation ordering

## Standard Behaviors

1. `TextFieldBindingBehavior`
2. `CheckBoxBindingBehavior`
3. `ValidationBorderBehavior`
4. `ValidationTooltipBehavior`
5. `HelpBehavior`
6. `RequiredMarkerBehavior`
7. `DirtyMarkerBehavior`
8. `SelectAllOnFocusBehavior`
9. `CommitOnEnterBehavior`

## Acceptance Criteria

- Behaviors install and uninstall deterministically.
- Behavior phases/order are respected.
- Conflicting behaviors fail fast.
- BehaviorScope closes all installed behaviors in reverse order.
- Behavior-based bindings pass the same tests as direct bindings.

---

# Stage 7 — Commands and Swing Actions

## Objective

Implement a type-safe command/action model and Swing adapters.

## Features

1. `Command`
2. `CommandSet`
3. `CommandFactory`
4. `ActionDescriptor`
5. `SwingActionAdapter`
6. enabled state binding
7. selected state binding for toggle actions
8. accelerator metadata
9. mnemonic metadata
10. key binding behavior
11. command button behavior
12. command menu item behavior

## Acceptance Criteria

- One command can back a button, menu item, toolbar button, and key binding.
- Updating command enabled state updates all bound Swing components.
- Toggle command selected state works for `JToggleButton` and `JCheckBoxMenuItem`.
- Key binding dispatches the same command handler.
- Command metadata is resource-key based, not raw string based in application code.

---

# Stage 8 — Annotation Processor Phase 1: Metamodel and Descriptors

## Objective

Generate typed metadata without generating full form models yet.

## Annotations

1. `@CoruscoForm`
2. `@TextField`
3. `@CheckBox`
4. `@ComboBox`
5. `@DateField`
6. `@Required`
7. `@Length`
8. `@DecimalRange`
9. `@IntRange`
10. `@Regex`
11. `@Help`
12. `@UiAction`
13. marker interfaces for key classes

## Generated Types

For `CustomerEdit`:

```text
CustomerEditFields
CustomerEditDescriptors
CustomerEditResources
CustomerEditProblems
CustomerEditActions
```

## Acceptance Criteria

- Generated field keys are type-safe.
- Generated descriptors contain labels/help/constraints.
- Invalid annotation combinations fail compilation.
- Processor uses `javax.lang.model`, not reflection.
- Processor is registered through `META-INF/services/javax.annotation.processing.Processor`.
- Gradle incremental annotation processing metadata is present.

---

# Stage 9 — Annotation Processor Phase 2: Generated Form Models

## Objective

Generate concrete form models from annotated records.

## Generated Types

For `CustomerEdit`:

```text
CustomerEditFormModel
```

## Generated Form Model Responsibilities

1. Create field models.
2. Install generated validators.
3. Expose public final field members or accessor methods.
4. Implement `toResult()`.
5. Implement reset.
6. Expose typed descriptors.
7. Avoid runtime reflection.

## Initial Supported Source Shape

Immutable records:

```java
@CoruscoForm(id = "customer")
public record CustomerEdit(
    @TextField @Required String name,
    @TextField @DecimalRange(min = "0.00") BigDecimal creditLimit,
    @CheckBox boolean active
) {}
```

Mutable bean support is deferred.

## Acceptance Criteria

- Generated model compiles and is readable.
- `toResult()` calls canonical record constructor directly.
- Generated validators are installed.
- No generated code uses reflection.
- Form model passes runtime form tests.

---

# Stage 10 — Annotation Processor Phase 3: View Contracts and Behavior Plans

## Objective

Generate view contracts and behavior installation plans.

## Generated Types

For `CustomerEdit`:

```text
CustomerEditView
CustomerEditBehaviorPlan
CustomerEditBindings
```

## View Contract Example

```java
public interface CustomerEditView {
    JTextField nameField();
    JTextField creditLimitField();
    JCheckBox activeBox();
}
```

## Behavior Plan Responsibilities

1. Bind text fields to generated form fields.
2. Install help behavior.
3. Install validation behavior.
4. Install required marker behavior.
5. Install dirty marker behavior where enabled.
6. Install default field UX behaviors.

## Acceptance Criteria

- A generated behavior plan can wire a complete simple dialog.
- Missing view methods cause compilation failure.
- Generated behavior plan contains no reflection.
- Behavior plan can be installed and disposed repeatedly.

---

# Stage 11 — Observable Collections and List Adapters

## Objective

Implement GlazedLists-inspired observable collections and Swing list/combo adapters.

## Features

1. `ObservableList<E>`
2. `ObservableArrayList<E>`
3. `ListChange<E>` sealed hierarchy
4. `ListChangeSet<E>`
5. batch mutation support
6. `FilteredList<E>`
7. `SortedList<E>`
8. `MappedList<S, T>`
9. `ObservableListModel<E>`
10. `ObservableComboBoxModel<E>`
11. first-class Glazed Lists interop adapters for mature `EventList`-based
    applications
12. EDT proxy list or EDT event dispatcher

## Acceptance Criteria

- Insert/delete/update/move events are precise.
- Batch events are preserved.
- Filtered and sorted views update correctly after source changes.
- Swing adapters fire correct Swing events.
- Glazed Lists can be adapted without treating it as a legacy/reflection escape
  hatch or forcing lossy event conversion.
- Background-originated changes can be marshalled to EDT through explicit adapter.

---

# Stage 12 — Table Model and Table Column Descriptors

## Objective

Implement typed table infrastructure.

## Features

1. `TableKey<R>`
2. `ColumnKey<R, V>`
3. `Column<R, V>`
4. `ColumnDescriptor<R, V>`
5. `ColumnDefaults`
6. `ColumnCapabilities`
7. `ObservableTableModel<R>`
8. editable columns with row updater/wither
9. table selection binding
10. table cell problem target
11. table cell validation decoration placeholder

## Acceptance Criteria

- `ObservableTableModel` fires precise row/cell events.
- Editable record row can be updated through generated wither/updater.
- Column classes are correct for Swing sorting/rendering.
- Selection binding converts view/model indices correctly.
- No arbitrary column-name strings in user API.

---

# Stage 13 — Annotation Processor Phase 4: Generated Table Columns

## Objective

Generate table metadata from annotated row records/specs.

## Annotations

1. `@CoruscoTable`
2. `@Column`
3. column help metadata
4. column persistence metadata
5. editable column metadata

## Generated Types

For `CustomerRow`:

```text
CustomerRowColumns
CustomerRowTableDescriptor
CustomerRowTableResources
CustomerRowTableBindings
```

## Acceptance Criteria

- Generated columns include stable keys.
- Generated columns include persistence IDs.
- Generated columns include default width/order/visibility.
- Generated table descriptor can create `ObservableTableModel`.
- Generated code supports read-only record rows.
- Editable record-row updater support is implemented or explicitly deferred.

---

# Stage 14 — Table State Persistence

## Objective

Persist and restore table column layout/state.

## Features

1. `TableState`
2. `ColumnState`
3. `SortState`
4. `TableStateStore`
5. `PreferencesTableStateStore`
6. `InMemoryTableStateStore`
7. `TableStateController`
8. table header column visibility menu
9. table state merge algorithm
10. schema version support
11. migration hook

## Acceptance Criteria

- Column order persists across table recreation.
- Column width persists across table recreation.
- Column visibility persists across table recreation.
- Unknown old columns are ignored.
- New columns are inserted according to default order.
- Widths are clamped to min/max.
- Sort state can be persisted and restored.
- State saves are debounced and flushed on disposal.

---

# Stage 15 — Help, Tooltips, Resources, and Accessibility

## Objective

Implement context help and static/dynamic tooltip composition.

## Features

1. `Resources`
2. `ResourceKey<T>` lookup
3. generated resource-key descriptors
4. `HelpService`
5. `HelpTopic`
6. `HelpBehavior`
7. `TooltipPolicy`
8. tooltip composition:
   - validation first
   - disabled reason
   - static help
   - F1 indicator
9. status-bar text behavior
10. accessible name/description behavior
11. table header tooltip behavior
12. table cell tooltip behavior

## Acceptance Criteria

- Field tooltips are generated from descriptors.
- Validation problems can override or augment static tooltip.
- F1 opens help topic for focused component.
- Table headers show column help tooltip.
- Accessible descriptions can be derived from the same descriptors.

---

# Stage 16 — Detachable and Loadable Models

## Objective

Adopt Wicket-inspired detachable model pattern for Swing data lifecycles.

## Features

1. `Detachable`
2. `LoadableValue<T>`
3. `DetachableValue<T>`
4. `LoadableList<E>` or adapter to `ObservableList<E>`
5. attach/detach lifecycle integration
6. refresh/invalidate semantics
7. master-detail loadable value helpers

## Acceptance Criteria

- Expensive value loads lazily.
- `detach()` releases cached data.
- Refresh invalidates and reloads.
- View/presenter lifecycle can detach inactive models.
- Detachable model tests prove data can be garbage collected after detach where practical.

---

# Stage 17 — Async Tasks and Async Validation

## Objective

Support business operations and server-side validation without blocking EDT.

## Features

1. `TaskService`
2. virtual-thread-backed default implementation
3. EDT-delivered callbacks
4. `UiTask<T>`
5. busy state values
6. cancellation token
7. stale-result generation counter helper
8. async validation API
9. busy overlay behavior using `JLayer`

## Acceptance Criteria

- Blocking task runs off EDT.
- Success/failure callbacks execute on EDT.
- Busy state updates correctly.
- Cancel suppresses callbacks where appropriate.
- Async validation ignores stale results after field changes.
- Busy overlay blocks input and disposes correctly.

---

# Stage 18 — Modal Dialog Framework

## Objective

Provide robust modal form dialog support.

## Features

1. `DialogResult<R>` sealed hierarchy
2. `FormDialog<P, R>` base/controller
3. OK/Cancel/Apply command integration
4. dirty-cancel confirmation hook
5. ESC handling
6. default button handling
7. commit active editor:
   - formatted fields
   - spinners
   - tables
8. validation summary and focus-first-problem
9. dialog lifecycle integration with presenter/behaviors/tasks

## Acceptance Criteria

- OK returns accepted result only when committable.
- Cancel returns cancelled result and does not mutate original object.
- Dirty cancel can ask confirmation.
- ESC follows cancel logic.
- Table cell editor is stopped before commit.
- Modal dialog can be opened/closed repeatedly without leaks.

---

# Stage 19 — Swing MVP Test Harness

## Objective

Provide WicketTester-inspired tools for testing generated bindings, behaviors, and presenters.

## Features

1. `SwingMvpTester<V, P>`
2. EDT-safe test execution utilities
3. component lookup by generated `ComponentKey`
4. action invocation by `ActionKey`
5. field input helpers
6. table selection helpers
7. problem assertions
8. behavior-installed assertions
9. table-state assertions
10. generated-source test helpers

## Example Test Style

```java
@Test
void creditLimitMustNotBeNegative() {
    tester.enterText(CustomerEditComponents.CREDIT_LIMIT_FIELD, "-1")
          .assertProblem(CustomerEditFields.CREDIT_LIMIT, CustomerProblems.NEGATIVE)
          .assertCommandEnabled(CustomerActionKeys.Save.class, false);
}
```

## Acceptance Criteria

- Tests can run headless where possible.
- Swing interactions are executed on EDT.
- Generated view contracts/components are testable.
- Behavior installation can be asserted.
- Processor tests can compile sample sources and inspect generated sources.

---

# Stage 20 — Example Application and Documentation

## Objective

Prove the framework in a realistic miniature business application.

## Example Domain

Customer management:

- customer search table;
- editable customer dialog;
- address sub-dialog;
- invoice lines table;
- async VAT validation;
- generated help/tooltips;
- persistent table state;
- save/reset/cancel actions;
- validation summary.

## Documentation Deliverables

1. README quickstart.
2. Architecture overview.
3. Annotation reference.
4. Behavior authoring guide.
5. Form model guide.
6. Table guide.
7. Command/action guide.
8. Dialog guide.
9. Testing guide.
10. Generated code examples.

## Acceptance Criteria

- Example application demonstrates all MVR features.
- Documentation shows handwritten and generated code side by side.
- A new developer can create a small form following the guide.

---

# Stage 21 — Packaging, API Polish, and First Preview Release

## Objective

Prepare a usable preview release.

## Tasks

1. Review public API names.
2. Freeze package structure for preview.
3. Add `module-info.java` where appropriate.
4. Generate Javadocs.
5. Produce source/javadoc jars.
6. Add Gradle publishing configuration.
7. Add semantic versioning policy.
8. Add binary compatibility policy.
9. Add changelog.
10. Tag `v0.1.0-preview`.

## Acceptance Criteria

- All examples compile against published artifacts.
- Generated docs are readable.
- No core API depends on optional/legacy reflection module.
- Preview release can be consumed from a local Maven repository.

---

## 10. Proposed Minimum Viable Release Scope

The MVR should include only features necessary to prove the architecture end-to-end.

### Include in MVR

1. Core observable values.
2. Field and text field models.
3. Form model core.
4. Validation/problem model.
5. Basic Swing bindings.
6. Behavior core.
7. Basic command/action model.
8. Annotation processor generating:
   - field keys;
   - descriptors;
   - form model for records;
   - view contract;
   - behavior plan.
9. Basic observable list.
10. Basic table model with generated columns.
11. Table state persistence.
12. Help/tooltips for fields and table headers.
13. Modal dialog result handling.
14. Test harness basics.
15. One complete example application.

### Exclude from MVR

1. Mutable bean support.
2. Runtime reflection adapter.
3. Full session-state framework.
4. Docking/window management.
5. Complex tree models.
6. Expression-language validation.
7. GUI builder integration.
8. Annotation-driven async action wrapping.
9. Advanced table grouping/pinning.
10. Full localization tooling.

---

## 11. Suggested Initial API Sketch

### 11.1 Form Record

```java
@CoruscoForm(id = "customer")
public record CustomerEdit(
    @TextField
    @Required
    @Length(max = 80)
    @Help(tooltip = CustomerHelp.NameTooltip.class, topic = CustomerHelp.NameTopic.class)
    String name,

    @TextField
    @DecimalRange(min = "0.00")
    @Help(tooltip = CustomerHelp.CreditLimitTooltip.class)
    BigDecimal creditLimit,

    @CheckBox
    boolean active
) {}
```

### 11.2 Presenter

```java
public final class CustomerEditPresenter
        extends AbstractFormPresenter<CustomerEdit, CustomerEditFormModel> {

    private final CustomerService service;

    public CustomerEditPresenter(CustomerEdit original, CustomerService service, FormServices services) {
        super(new CustomerEditFormModel(original, services));
        this.service = service;
    }

    @UiAction(
        key = CustomerActionKeys.Save.class,
        text = CustomerActionText.Save.class,
        tooltip = CustomerActionText.SaveTooltip.class,
        accelerator = @Accelerator(key = Key.S, modifiers = { KeyModifier.MENU_SHORTCUT }),
        enabledBy = StandardEnablement.FORM_COMMITTABLE
    )
    public void save() {
        if (!form().isCommittable()) {
            form().focusFirstProblem();
            return;
        }

        CustomerEdit result = form().toResult();
        service.save(result);
        accept(result);
    }
}
```

### 11.3 View

```java
public final class CustomerEditPanel extends JPanel implements CustomerEditView {
    private final JTextField nameField = new JTextField(30);
    private final JTextField creditLimitField = new JTextField(12);
    private final JCheckBox activeBox = new JCheckBox();

    private final BehaviorScope behaviors = new BehaviorScope();

    public void bind(CustomerEditPresenter presenter, BehaviorFactory behaviorFactory) {
        CustomerEditBehaviorPlan.install(this, presenter, behaviorFactory, behaviors);
    }

    @Override
    public JTextField nameField() {
        return nameField;
    }

    @Override
    public JTextField creditLimitField() {
        return creditLimitField;
    }

    @Override
    public JCheckBox activeBox() {
        return activeBox;
    }

    @Override
    public void removeNotify() {
        behaviors.close();
        super.removeNotify();
    }
}
```

### 11.4 Generated Form Model Shape

```java
public final class CustomerEditFormModel extends AbstractFormModel<CustomerEdit> {
    public final TextFieldModel<String> name;
    public final TextFieldModel<BigDecimal> creditLimit;
    public final FieldModel<Boolean> active;

    public CustomerEditFormModel(CustomerEdit original, FormServices services) {
        super(CustomerEdit.class, original, services);

        this.name = fields().text(CustomerEditFields.NAME, original.name(), Converters.string());
        this.creditLimit = fields().text(
            CustomerEditFields.CREDIT_LIMIT,
            original.creditLimit(),
            services.converters().bigDecimal()
        );
        this.active = fields().value(CustomerEditFields.ACTIVE, original.active());

        installGeneratedValidators();
    }

    @Override
    public CustomerEdit toResult() {
        requireCommittable();
        return new CustomerEdit(name.value(), creditLimit.value(), active.value());
    }
}
```

---

## 12. Risks and Mitigations

### 12.1 Risk: Over-Generalization

The framework may grow into a large platform.

Mitigation:

- Keep MVR small.
- Prefer narrow primitives.
- Reject GUI-builder ambitions.
- Keep optional features in separate modules.

### 12.2 Risk: Annotation Noise

Business forms could become annotation-heavy.

Mitigation:

- Use sensible defaults.
- Derive resource/help keys by convention.
- Allow Java DSL for complex behavior.
- Generate boilerplate, not business logic.

### 12.3 Risk: Processor Complexity

Annotation processors are easy to make brittle.

Mitigation:

- Start with metamodel generation only.
- Add generated form models after APIs stabilize.
- Test processors with sample source compilation.
- Keep generated code simple.
- Avoid global registries early.

### 12.4 Risk: EDT Violations

Framework abstractions may hide threading errors.

Mitigation:

- Add EDT assertions in debug mode.
- Route task callbacks through EDT gateway.
- Document EDT ownership clearly.
- Test bindings on EDT.

### 12.5 Risk: Behavior Conflicts

Multiple behaviors may fight over borders, tooltips, action maps, or table headers.

Mitigation:

- Use behavior phases and ordering.
- Add behavior keys and cardinality.
- Provide composed tooltip and composed border decorators.
- Fail fast on conflicting primary bindings.

### 12.6 Risk: Table State Upgrade Bugs

Persisted column state may break after schema changes.

Mitigation:

- Persist stable generated column IDs.
- Add schema version.
- Define merge algorithm.
- Ignore unknown old columns.
- Insert new columns by default order.
- Test migration cases.

---

## 13. Definition of Done

A feature is done when:

1. Public API is documented.
2. Unit tests cover nominal and edge cases.
3. Disposal/lifecycle behavior is tested.
4. EDT expectations are documented and tested where relevant.
5. Generated code, if any, has golden-source tests.
6. Generated code has behavioral tests, not only text comparisons.
7. Example usage exists if the feature is user-facing.
8. No runtime reflection is introduced into core modules.
9. No arbitrary string contract is introduced into public API.
10. The feature composes with `BehaviorScope` or `BindingScope` if it installs listeners/resources.

---

## 14. Immediate Task Sequence

The initial repository, lifecycle, observable value, and typed key tasks are
complete. The next implementation tasks should proceed in this order:

1. Implement `Problem`, `ProblemSet`, and `ProblemFilter`.
2. Implement `FieldModel` and `TextFieldModel` without Swing.
3. Implement `JTextField` binding manually.
4. Implement `ViewBehavior`, `BehaviorScope`, and `ValidationBorderBehavior`.
5. Add the first annotation: `@CoruscoForm`.
6. Generate only `CustomerEditFields` for the first processor spike.
7. Build a tiny `CustomerEdit` example using a mix of generated keys and
   handwritten model.
8. Expand generation only after the handwritten APIs feel stable.

---

## 15. Strategic Summary

The framework should be built around one central idea:

```text
Compile-time generated, type-safe presentation schema
    + small explicit runtime primitives
    + lifecycle-aware Swing behaviors
    + precise observable models
    = maintainable business Swing applications
```

The first releases should not attempt to automate everything. They should make the correct architecture easy:

- presenters own state and commands;
- views own Swing components;
- generated descriptors connect them;
- behaviors enrich components;
- bindings are disposable;
- validation produces typed problems;
- tables use stable generated columns;
- dialogs return typed results;
- background work returns to the EDT;
- generated code is readable, debuggable, and boring.

That combination is the durable path: JGoodies-style presentation discipline, GlazedLists-style event precision, BSAF-style command/lifecycle vocabulary, SwingX-style UX enrichment, and Wicket-style behaviors — but specialized for modern Java and Swing rather than copied literally.
