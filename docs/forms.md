# Corusco Form Model Guide

Form models are Swing-free presentation models for editing immutable business
values. They sit between a domain/edit record and Swing bindings, preserving
invalid intermediate input, dirty/touched state, parse problems, validation
problems, and guarded result creation.

Use form models when a screen edits a transaction buffer rather than mutating a
domain object directly:

```text
domain value
    -> immutable edit record
        -> form model
            -> field models and validation
        -> toResult()
    -> accepted edit record
    -> application service
```

## Core Types

| Type | Role |
| --- | --- |
| `FieldModel<O, T>` | Semantic field value with typed `FieldKey`, dirty state, touched state, reset, and baseline acceptance. |
| `TextFieldModel<O, T>` | Text-editable field with raw text, parse state, parse problems, and semantic value preservation. |
| `ParseState<T>` | Parsed or failed parse state for a text field. |
| `FormModel<R>` | Non-Swing form contract: problems, committability, reset, baseline acceptance, and result creation. |
| `AbstractFormModel<R>` | Base class for handwritten and generated form models. |
| `RuleSet<M>` | Typed validation rules with dependency metadata. |
| `Validators` | Generated-compatible field validators for required, length, regex, decimal range, integer range, and date checks. |

The owner type parameter `O` is usually the edit record type. It exists so keys
such as `TextFieldKey<CustomerEdit, BigDecimal>` cannot be accidentally applied
to another form.

See [Dialog Guide](dialogs.md) for the OK/Apply/Cancel controller layer that
turns committable form models into typed dialog results.

## Text Fields Preserve Invalid Input

Text fields keep raw text and semantic value separate. Invalid user input is a
valid UI state, not an exception path.

```java
TextFieldModel<CustomerEdit, BigDecimal> creditLimit =
        new TextFieldModel<>(
                CREDIT_LIMIT,
                new BigDecimal("10.00"),
                Converters.bigDecimal(EmptyTextPolicy.REJECT)
        );

creditLimit.setRawText("12,", ChangeOrigin.USER);

String rawText = creditLimit.rawText().value();       // "12,"
BigDecimal semantic = creditLimit.value();            // still 10.00
boolean hasParseError = creditLimit.problems().hasErrors();
```

On parse success, the semantic field value changes and parse problems clear. On
parse failure, the raw text changes, the previous semantic value is preserved,
the field is marked touched, and a typed parse problem targets the field key.

## Handwritten Form Models

Handwritten forms extend `AbstractFormModel<R>`, register fields in the
constructor, and implement `createResult()`.

```java
final class CustomerEditForm extends AbstractFormModel<CustomerEdit> {

    final TextFieldModel<CustomerEdit, String> name;
    final TextFieldModel<CustomerEdit, BigDecimal> creditLimit;

    CustomerEditForm(CustomerEdit original) {
        this.name = register(new TextFieldModel<>(
                CustomerFields.NAME,
                original.name(),
                Converters.string()
        ));
        this.creditLimit = register(new TextFieldModel<>(
                CustomerFields.CREDIT_LIMIT,
                original.creditLimit(),
                Converters.bigDecimal(EmptyTextPolicy.REJECT)
        ));
    }

    @Override
    protected CustomerEdit createResult() {
        return new CustomerEdit(name.value(), creditLimit.value());
    }
}
```

`toResult()` is final in `AbstractFormModel`. It checks `isCommittable()` first
and throws `UncommittableFormException` instead of producing a partially invalid
record.

## Generated Form Models

Annotating a record with `@SwingForm` generates the same shape:

```java
@SwingForm(id = "generated-customer")
record GeneratedCustomerEdit(
        @TextField @Required @Length(max = 80) String name,
        @TextField @DecimalRange(min = "0.00") BigDecimal creditLimit,
        @TextField @IntRange(min = 0, max = 120) Integer age,
        @DateField LocalDate validFrom,
        @ComboBox GeneratedCustomerType type,
        @CheckBox boolean active
) {
}
```

The generated `GeneratedCustomerEditFormModel`:

- extends `AbstractFormModel<GeneratedCustomerEdit>`;
- exposes ordinary field members such as `name`, `creditLimit`, `type`, and
  `active`;
- registers `TextFieldModel` for text/date fields and `FieldModel` for semantic
  fields such as combo boxes and checkboxes;
- exposes descriptors in record component order;
- builds a `RuleSet` from supported validation annotations;
- creates a replacement immutable record in `createResult()`.

```java
GeneratedCustomerEdit original = new GeneratedCustomerEdit(
        "Alice",
        new BigDecimal("10.00"),
        30,
        LocalDate.parse("2026-01-01"),
        GeneratedCustomerType.RETAIL,
        true
);
GeneratedCustomerEditFormModel model = new GeneratedCustomerEditFormModel(original);

model.name.setRawText("Bob", ChangeOrigin.USER);
model.creditLimit.setRawText("25.50", ChangeOrigin.USER);
model.age.setRawText("45", ChangeOrigin.USER);
model.active.setValue(false, ChangeOrigin.USER);

GeneratedCustomerEdit committed = model.toResult();
```

Generated form models are still plain Java. Read them during review; they should
use direct field members, generated keys, descriptors, validators, and record
constructors.

Field identity flows through generated `FieldKey` and `TextFieldKey` constants.
Do not introduce JavaBeans property paths or reflective field lookup to connect
forms to bindings, validation, or tests.

## Dirty, Touched, Reset, and Baseline

`FieldModel` tracks two independent states:

- dirty: current semantic value differs from the accepted baseline;
- touched: user or model code has interacted with the field.

`reset()` restores registered fields to their original baseline, clears touched
state, refreshes dirty state, resets raw text, and clears parse problems.

`acceptCurrentValues()` makes current semantic values the new dirty-state
baseline. Use it after a successful save/apply operation when the form should
continue editing the accepted values.

## Problems and Committability

`AbstractFormModel.problems()` aggregates:

1. parse problems from registered `TextFieldModel` instances;
2. validation problems returned by `validationProblems()`.

`isCommittable()` returns `false` when any aggregated problem has error
severity. Warnings and informational problems can be represented in `ProblemSet`,
but they do not block result creation unless they are errors.

## Validation Rules

Use `RuleSet` for synchronous validation. Field rules depend on typed keys, not
field-name strings.

```java
private final RuleSet<CustomerForm> rules = RuleSet.<CustomerForm>builder()
        .field(CustomerFields.NAME.asFieldKey(),
                form -> form.name,
                Validators.required("Name is required"))
        .field(CustomerFields.CREDIT_LIMIT.asFieldKey(),
                form -> form.creditLimit,
                Validators.decimalRange(
                        BigDecimal.ZERO,
                        new BigDecimal("1000.00"),
                        "Credit limit is too high"
                ))
        .form(List.of(CustomerFields.NAME.asFieldKey(), CustomerFields.CREDIT_LIMIT.asFieldKey()), form -> {
            if (form.name.value().isBlank() && form.creditLimit.value().compareTo(BigDecimal.ZERO) > 0) {
                return ProblemSet.of(Problem.validation(
                        CREDIT_LIMIT_REQUIRES_NAME,
                        ProblemSeverity.ERROR,
                        ProblemTarget.form(),
                        "Credit limit requires a customer name"
                ));
            }
            return ProblemSet.empty();
        })
        .build();

@Override
protected ProblemSet validationProblems() {
    return rules.validateAll(this);
}
```

Field rules for `TextFieldModel` skip validation when that field already has
parse errors. This keeps parse failures and semantic validation separate.

`ValidationTiming` is metadata. The current core validates synchronously when
called; bindings and generated plans can use timing later to decide whether a
rule belongs to change-time or commit-time validation.

## Async Validation

Use `AsyncFieldValidation` for server-side or expensive checks. It observes a
semantic value, submits validation work through a `TaskService`, and ignores
stale callbacks using a generation counter.

Async validation is Swing-free. When attached to Swing screens, own it with the
same lifecycle scope as the view or presenter and close it when the screen is
disposed.

## Swing Binding Boundary

Form models do not depend on Swing. Bind them to components through
`corusco-swing`:

- low-level bindings in `BindingFactory`;
- behavior composition in `StandardBehaviors`;
- generated behavior plans such as `CustomerEditBehaviorPlan`.

This keeps model tests fast and headless. Swing tests should verify binding and
behavior wiring separately.

## Testing Form Models

Test handwritten and generated form models without opening windows:

- invalid raw text preserves the previous semantic value;
- parse problems block `toResult()`;
- validation problems aggregate with parse problems;
- `reset()` restores original values and clears parse problems;
- `acceptCurrentValues()` clears dirty state after save/apply;
- generated models commit to immutable replacement records.

See `FieldModelExample`, `ValidationExample`, and
`GeneratedFormModelExample` for compiling scenarios with method-body comments.

## Current Limits

- Generated form models currently target non-generic records.
- Generated text fields support `String`, `Integer`/`int`, `BigDecimal`, and
  `LocalDate`.
- Generated combo boxes expose semantic `FieldModel` values but do not yet
  generate option-source metadata.
- Cross-field generated validation is not annotation-driven; use handwritten
  `RuleSet` rules.
- Form models are synchronous unless explicit async validation controllers are
  attached by application code.
