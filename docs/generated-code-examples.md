# Corusco Generated Code Examples

Generated Corusco code should be easy to read in the build directory. It is
ordinary Java source that calls typed keys, descriptors, validators, record
accessors, constructors, Swing behavior factories, and binding helpers directly.
It must not rely on runtime annotation scanning, JavaBeans property paths, or
reflection.

The examples module contains the current generated-code walkthroughs. Build the
project and inspect generated sources under:

```text
corusco-examples/build/generated/sources/annotationProcessor/java/main/cz/auderis/corusco/examples
```

## Example Map

| Annotated source | Generated companions | Runtime example |
| --- | --- | --- |
| `GeneratedCustomerEdit` | `GeneratedCustomerEditFields`, `Resources`, `Problems`, `Descriptors`, `FormModel`, `View`, `BehaviorPlan` | `GeneratedMetadataExample`, `GeneratedFormModelExample`, `GeneratedViewPlanExample` |
| `GeneratedCustomerRow` | `GeneratedCustomerRowColumns`, `TableResources`, `TableDescriptor`, `TableBindings` | `GeneratedTableColumnsExample` |
| `GeneratedActionMetadataExample` | `GeneratedActionMetadataExampleActions` | `GeneratedActionMetadataExample` |

Each runtime example has a matching test under `corusco-examples/src/test`.
Those tests assert behavior through public APIs, not private generated
implementation details.

`CustomerManagementExample` composes the generated form/table/action pieces
with dialog controllers, table state, validation summary, invoice-line editing,
an address sub-dialog, and async VAT validation into one headless miniature
business flow.

## Form Metadata

`GeneratedCustomerEdit` is a compact annotated record:

```java
@SwingForm(id = "generated-customer")
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
direct model field references, and direct `StandardBehaviors` factory calls.

`GeneratedViewPlanExample` mirrors the generated plan inside the example source
set because javac cannot reliably resolve a type generated from the same source
set during attribution. Treat that helper as a workaround for example
compilation, not a different runtime architecture.

## Table Metadata and Bindings

`GeneratedCustomerRow` demonstrates generated table metadata:

```java
@SwingTable(id = "generated-customer-table")
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

`@UiAction` methods generate action descriptors only:

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
`ResourceKey<String>`, and `ActionDescriptor` constants. It does not invoke the
annotated method. Presenter code creates command instances from descriptors and
owns invocation.

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

- Generated forms and tables currently target non-generic records.
- Generated action metadata does not generate method invocation glue.
- Generated combo boxes do not yet include option-source metadata.
- Generated dialog shells, menus, toolbars, and native-window factories are not
  generated yet.
- Example source sometimes mirrors generated code when same-source-set javac
  attribution would otherwise make examples brittle.
