# Corusco Generated Code Examples

Generated Corusco code should be easy to read in the build directory. It is
ordinary Java source that calls typed keys, descriptors, validators, record
accessors, constructors, and, for packages annotated with `@SwingCompanionPackage`, Swing
behavior factories and binding helpers directly. It must not rely on runtime
annotation scanning, JavaBeans property paths, or reflection.

The examples module contains the current generated-code walkthroughs. Build the
project and inspect generated sources under:

```text
corusco-examples/build/generated/sources/annotationProcessor/java/main/cz/auderis/corusco/examples/generated
```

## Example Map

| Annotated source | Generated companions | Runtime example |
| --- | --- | --- |
| `examples.generated.GeneratedCustomerEdit` | `GeneratedCustomerEditFields`, `Resources`, `Problems`, `Descriptors`, `FormModel`, `PresentationModel`, `Options`; `@SwingCompanionPackage` also generates `View`, `BehaviorPlan`, `Bindings` | `examples.generated.GeneratedMetadataExample`, `examples.forms.GeneratedFormModelExample`, `examples.generated.GeneratedViewPlanExample` |
| `examples.generated.AbstractCustomerProfile` and `examples.generated.SecuritySettings` | Generated child `Fields`, `Descriptors`, `FormModel`, and `PresentationModel` classes; `SecuritySettings` also declares component state and dependency metadata | `examples.generated.MultiFormDialogSessionExample` |
| `examples.generated.GeneratedCustomerRow` | `GeneratedCustomerRowColumns`, `TableResources`, `TableDescriptor`; `@SwingCompanionPackage` also generates `TableBindings` | `examples.tables.GeneratedTableColumnsExample` |
| `examples.generated.GeneratedActionMetadataExample` | `GeneratedActionMetadataExampleActions` | `examples.generated.GeneratedActionMetadataExample` |

Each runtime example has a matching test under the corresponding package in
`corusco-examples/src/test`. Those tests assert behavior through public APIs,
not private generated implementation details.

`examples.generated.CustomerManagementExample` composes the generated
form/table/action pieces with dialog controllers, table state, validation
summary, invoice-line editing, an address sub-dialog, and async VAT validation
into one headless miniature business flow.

`examples.generated.MultiFormDialogSessionExample` shows a smaller
multi-form transaction. It composes generated child form and presentation
models inside a handwritten `AbstractCompositeFormModel` parent, derives Apply
and Revert action state from two explicit dirty policies, routes child field
problems through `ProblemFocusResolver`, and keeps generated Swing companions
optional.

## Form Metadata

`GeneratedCustomerEdit` is a compact annotated record:

```java
@CoruscoForm(id = "generated-customer")
record GeneratedCustomerEdit(
        @TextField @Required @Length(max = 80) @Regex("[A-Za-z ]+")
        @Help(topic = "generated-customer/name")
        String name,
        @TextField @DecimalRange(min = "0.00") BigDecimal creditLimit,
        @TextField @IntRange(min = 0, max = 120) Integer age,
        @DateField LocalDate validFrom,
        @ComboBox GeneratedCustomerType type,
        @CheckBox boolean active
) {
}
```

The generated metadata classes expose stable constants:

- `GeneratedCustomerEditFields`: typed field keys;
- `GeneratedCustomerEditResources`: resource keys;
- `GeneratedCustomerEditProblems`: problem codes;
- `GeneratedCustomerEditDescriptors`: field descriptors and constraint
  descriptors.

`GeneratedMetadataExample` reads those constants so reviewers can see the
generated contract without opening a Swing view.

## Form Model

`GeneratedCustomerEditFormModel` extends `AbstractFormModel` and exposes one
ordinary field-model member per record component:

```java
GeneratedCustomerEditFormModel model = new GeneratedCustomerEditFormModel(original);

model.name.setRawText("Bob", ChangeOrigin.USER);
model.creditLimit.setRawText("25.50", ChangeOrigin.USER);
model.active.setValue(false, ChangeOrigin.USER);

GeneratedCustomerEdit committed = model.toResult();
```

Generated form models should remain direct:

- constructors register `TextFieldModel` and `FieldModel` instances;
- `descriptors()` returns generated descriptors in record component order;
- `buildRules()` wires supported validation annotations into a typed `RuleSet`;
- `createResult()` calls the immutable record constructor.

Generated form models can also be composed. A handwritten parent session can
extend `AbstractCompositeFormModel<R>`, register generated child models in
user-visible order, add cross-child validation, and assemble one typed result.
Keep child `toResult()` calls in the parent assembler, not in parent validation
unless the child is known to be committable.

Generated presentation models own visual and session state for generated
bindings. A binding facade can accept an explicit presentation model, or create
one from a form model for simple callers:

```java
GeneratedCustomerEditPresentationModel presentation =
        new GeneratedCustomerEditPresentationModel(model);
GeneratedCustomerEditBindings.install(view, presentation, scope);
```

For multi-form dialogs, parent presenters should compose child presentation
models rather than replacing them. Dialog action state, tab selection, and
problem routing can live beside the child presenters, while the semantic result
continues to contain only form values.

The generated source is intentionally readable enough to review. If generated
formatting or naming makes the source difficult to inspect, fix the processor
instead of hiding the output behind more runtime indirection.

## View Contracts and Behavior Plans

`GeneratedCustomerEditView` is a Swing interface:

```java
public interface GeneratedCustomerEditView {
    JTextField nameField();
    JTextField creditLimitField();
    JTextField ageField();
    JTextField validFromField();
    JComboBox<GeneratedCustomerType> typeCombo();
    JCheckBox activeBox();
}
```

`GeneratedCustomerEditBehaviorPlan` installs explicit behavior lists against
that interface. The generated plan should be boring: direct accessor calls,
direct presentation/form model references, and direct `StandardBehaviors`
factory calls. `GeneratedCustomerEditBindings.install(view, presentation,
scope)` is the public facade to call from application code; it delegates to the
generated behavior plan so generated binding APIs have the same shape as table
bindings.

For enum-valued combo boxes, `GeneratedCustomerEditOptions` exposes a
declaration-ordered immutable option list. Non-enum option sources remain
application-owned because loading, localization, and disabled-option policy are
outside the annotation model.

`GeneratedViewPlanExample` mirrors the generated plan inside the example source
set because javac cannot reliably resolve a type generated from the same source
set during attribution. Treat that helper as a workaround for example
compilation, not a different runtime architecture.

## Table Metadata and Bindings

`GeneratedCustomerRow` demonstrates generated table metadata:

```java
@CoruscoTable(id = "generated-customer-table")
record GeneratedCustomerRow(
        @Column(
                persistenceId = "generated-customer-table/customer-name",
                width = 180,
                minWidth = 80,
                maxWidth = 320,
                editable = true
        )
        @Help(tooltip = "generated-customer-table/name/help", topic = "generated-customer-table/name")
        String name,
        @Column(width = 80, sortable = false) int orders
) {
}
```

Generated table companions provide:

- typed table and column keys;
- column descriptors with resource, help, capability, default, and persistence
  metadata;
- editable column updater helpers that create replacement records;
- a table descriptor;
- binding helpers for model installation and selection binding.

`GeneratedTableColumnsExample` installs the generated descriptor into
`ObservableTableModel`, adapts a Glazed Lists `EventList`, persists table state,
uses the header visibility menu, edits a generated column, and binds selection.

## Action Metadata

`@UiAction` methods generate action descriptors and optional command factories:

```java
@UiAction(
        id = "generated-customer/save",
        tooltip = "generated-customer/save/tooltip",
        mnemonic = 83
)
void save() {
}
```

`GeneratedActionMetadataExampleActions` exposes `ActionKey`,
`ResourceKey<String>`, and `ActionDescriptor` constants, ordered descriptor
lists for menu/toolbar assembly, and factories such as `saveCommand(owner)` and
`commands(owner)`. The factories are additive: code that wants descriptor-only
usage can still create commands manually.

## Review Checklist

When reviewing generated code, check that it:

- uses typed keys, descriptors, and direct Java calls;
- has deterministic type names and member names;
- keeps stable ids separate from JavaBeans property paths;
- builds immutable replacement records rather than mutating JavaBean rows;
- installs behaviors through lifecycle scopes;
- uses generated binding helpers instead of duplicating table wiring;
- remains formatted and readable enough for a newcomer to inspect;
- has both generated-source tests and runtime behavior tests where behavior is
  nontrivial.

## Current Limits

- Generated forms currently target non-generic records and abstract classes;
  generated tables currently target non-generic records.
- Generated combo-box and radio-group option metadata is limited to enum-valued
  fields.
- Generated menu and toolbar metadata is declaration-ordered action grouping,
  not a full menu layout framework.
- Native dialog shell support is runtime API, not generated source.
- Example source sometimes mirrors generated code when same-source-set javac
  attribution would otherwise make examples brittle.
