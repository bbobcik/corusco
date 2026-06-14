# Corusco Annotation Reference

Corusco annotations are compile-time inputs for `corusco-processor`. They are
`SOURCE` retained and are read through `javax.lang.model` during javac
annotation processing. Runtime framework code must not scan them reflectively.

Use annotations to describe stable UI contracts. Put business behavior,
cross-field validation, persistence, and presenter logic in ordinary Java code.

## Package Layout

Annotation types are grouped by authoring concern:

| Package | Use for |
| --- | --- |
| `cz.auderis.corusco.annotations.form` | `@SwingForm` and field-kind annotations such as `@TextField`, `@DateField`, `@ComboBox`, and `@CheckBox`. |
| `cz.auderis.corusco.annotations.table` | `@SwingTable` and `@Column`. |
| `cz.auderis.corusco.annotations.validation` | Field constraints such as `@Required`, `@Length`, `@Regex`, `@DecimalRange`, and `@IntRange`. |
| `cz.auderis.corusco.annotations.command` | `@UiAction` action descriptor metadata. |
| `cz.auderis.corusco.annotations.help` | `@Help` tooltip and help-topic metadata. |

The root `cz.auderis.corusco.annotations` package is kept for module-level
support only. Import the package that matches the annotated source element.

## Stable ID Rules

IDs become typed-key IDs, resource IDs, problem-code IDs, and persisted table
state IDs. The processor currently accepts only:

- letters;
- digits;
- dots;
- underscores;
- dashes;
- slashes.

Examples:

```java
"customer/name"
"customer-search/main-name"
"customer.save.text"
```

Avoid JavaBeans-style property paths such as `customer.address.city` unless the
dots are genuinely part of a stable resource namespace. Prefer slash-separated
UI identity tokens for generated metadata.

## Form Annotations

### `@SwingForm`

Target: type. Supported source shape: non-generic record.

Required attributes:

| Attribute | Meaning |
| --- | --- |
| `id` | Stable form prefix used for generated field, resource, problem, and component IDs. |

Generated companions for `CustomerEdit`:

- `CustomerEditFields`
- `CustomerEditResources`
- `CustomerEditProblems`
- `CustomerEditDescriptors`
- `CustomerEditFormModel`
- `CustomerEditView`
- `CustomerEditBehaviorPlan`

```java
@SwingForm(id = "customer")
record CustomerEdit(
        @TextField @Required String name,
        @TextField @DecimalRange(min = "0.00") BigDecimal creditLimit,
        @CheckBox boolean active
) {
}
```

Current processor limits:

- the annotated type must be a record;
- generic records are rejected;
- at least one component must use a field-kind annotation;
- a component may use only one field-kind annotation.

### Field-Kind Annotations

Exactly one field-kind annotation may appear on a generated form component.

| Annotation | Component type | Generated model | Generated view method |
| --- | --- | --- | --- |
| `@TextField` | `String`, `Integer`, `int`, `BigDecimal`, or `LocalDate` | `TextFieldModel` | `JTextField <name>Field()` |
| `@DateField` | `LocalDate` | `TextFieldModel` | `JTextField <name>Field()` |
| `@ComboBox` | declared type, usually an enum | `FieldModel` | `JComboBox<T> <name>Combo()` |
| `@CheckBox` | `boolean` or `Boolean` | `FieldModel` | `JCheckBox <name>Box()` |

`@DateField` currently uses a text-field model with the built-in `LocalDate`
converter. Use `@TextField` for `LocalDate` only when the screen should behave
like a generic text field; use `@DateField` when the intent is date-specific UI.

### Field Metadata and Constraints

Metadata annotations require a field-kind annotation on the same record
component.

| Annotation | Applies to | Attributes | Generated effect |
| --- | --- | --- | --- |
| `@Required` | any generated form field | none | required constraint and problem code |
| `@Length` | `@TextField String` | `min` default `0`, required `max` | length constraint and problem code |
| `@Regex` | `@TextField String` | required `value` | regex constraint and problem code |
| `@DecimalRange` | `@TextField BigDecimal` | `min`, `max`, both optional but not both blank | decimal range constraint and problem code |
| `@IntRange` | `@TextField Integer` or `int` | `min`, `max` | integer range constraint and problem code |
| `@Help` | generated form fields and table columns | `tooltip`, `topic` | resource key and/or help topic metadata |

Validation checks performed by the processor include:

- `@Length` requires `0 <= min <= max`;
- `@Regex` must not be blank;
- `@DecimalRange` requires at least one valid decimal bound and `min <= max`;
- `@IntRange` requires `min <= max`;
- `@Help.tooltip` and `@Help.topic` must use stable IDs when present.

Generated form models install runtime validation rules for required, length,
regex, decimal range, and integer range constraints. More complex validation
belongs in the typed Java validation DSL.

See [Form Model Guide](forms.md) for how generated models use fields, parse
state, validation rules, and guarded result creation.

## Table Annotations

### `@SwingTable`

Target: type. Supported source shape: non-generic record.

Required attributes:

| Attribute | Meaning |
| --- | --- |
| `id` | Stable table prefix used for generated table and default column IDs. |

Generated companions for `CustomerRow`:

- `CustomerRowColumns`
- `CustomerRowTableResources`
- `CustomerRowTableDescriptor`
- `CustomerRowTableBindings`

```java
@SwingTable(id = "customer/search")
record CustomerRow(
        @Column(width = 180, editable = true) String name,
        @Column(width = 80, sortable = false) int orders
) {
}
```

Current processor limits:

- the annotated type must be a record;
- generic records are rejected;
- at least one record component must use `@Column`;
- column value types must be primitive or declared types;
- generated row updater helpers call the record constructor for editable
  columns.

### `@Column`

Target: record component under `@SwingTable`.

| Attribute | Default | Meaning |
| --- | --- | --- |
| `id` | `<table-id>/<component-kebab-name>` | Stable column key ID. |
| `header` | `<column-id>/header` | Header resource key ID. |
| `tooltip` | blank | Optional tooltip resource key ID. |
| `persistenceId` | `<column-id>` | Stable serialized table-state ID. |
| `width` | `120` | Default width in pixels. |
| `minWidth` | `24` | Minimum restored width. |
| `maxWidth` | `Integer.MAX_VALUE` | Maximum restored width. |
| `order` | record component order | Default visual order. Negative values use component order. |
| `visible` | `true` | Initial visibility. |
| `sortable` | `true` | Sort capability metadata. |
| `filterable` | `true` | Filter capability metadata. |
| `hideable` | `true` | Visibility-menu capability metadata. |
| `editable` | `false` | Whether generated columns call an updater when edited. |

Processor checks include:

- IDs must use the stable ID grammar;
- duplicate column IDs in the same table are rejected;
- `width > 0`;
- `minWidth > 0`;
- `minWidth <= width`;
- `width <= maxWidth`;
- `@Column.tooltip` and `@Help.tooltip` cannot both declare table tooltip IDs.

Use `@Help(topic = "...")` on a column when the descriptor should carry a help
topic. Use either `@Column(tooltip = "...")` or `@Help(tooltip = "...")`, not
both.

See [Table Guide](tables.md) for how generated column descriptors are installed
in Swing, how Glazed Lists row sources are adapted, and how table-state
persistence uses stable column ids.

## Action Annotations

### `@UiAction`

Target: method. Supported source shape: no-argument `void` method enclosed by a
type.

Generated companion for `CustomerPresenter`:

- `CustomerPresenterActions`

| Attribute | Default | Meaning |
| --- | --- | --- |
| `id` | required | Stable action key ID. |
| `text` | `<id>/text` | Visible text resource key ID. |
| `tooltip` | blank | Optional tooltip resource key ID. |
| `mnemonic` | `0` | Optional key code for mnemonic metadata. |
| `acceleratorKey` | `0` | Optional key code for accelerator metadata. |
| `acceleratorModifiers` | `0` | Optional modifier mask. Requires `acceleratorKey`. |
| `selectable` | `false` | Generate toggle-style action descriptor. |

```java
final class CustomerPresenter {

    @UiAction(
            id = "customer/save",
            text = "customer/save/text",
            tooltip = "customer/save/tooltip",
            mnemonic = 83
    )
    void save() {
    }

    @UiAction(id = "customer/active", selectable = true)
    void toggleActive() {
    }
}
```

Processor checks include:

- the annotated element must be a method;
- the method must have no parameters;
- the method must return `void`;
- action, text, and tooltip IDs must use the stable ID grammar;
- duplicate action IDs within one owner type are rejected;
- nonzero `acceleratorModifiers` require nonzero `acceleratorKey`.

Generated action factories call annotated no-argument methods directly on an
owner instance. Descriptor-only usage remains available when a presenter needs
custom command construction.

See [Command and Action Guide](commands.md) for how generated action descriptors
become presenter-owned commands and Swing entry points.

## Generated Names

Generated type names are deterministic and live in the same package as the
annotated source type.

| Source | Generated names |
| --- | --- |
| `@SwingForm CustomerEdit` | `CustomerEditFields`, `CustomerEditResources`, `CustomerEditProblems`, `CustomerEditDescriptors`, `CustomerEditFormModel`, `CustomerEditView`, `CustomerEditBehaviorPlan`, `CustomerEditBindings`, `CustomerEditOptions` |
| `@SwingTable CustomerRow` | `CustomerRowColumns`, `CustomerRowTableResources`, `CustomerRowTableDescriptor`, `CustomerRowTableBindings` |
| `@UiAction` methods in `CustomerPresenter` | `CustomerPresenterActions` |

Record component names become constant names and method names by convention:

- `creditLimit` becomes `CREDIT_LIMIT`;
- field IDs default to `customer/credit-limit`;
- text view methods default to `creditLimitField()`;
- checkbox view methods default to `activeBox()`;
- combo-box view methods default to `typeCombo()`.

## Good Usage Patterns

- Keep annotation values stable once screens are released; IDs may be persisted
  in table state or referenced by resource bundles.
- Prefer generated descriptors over hand-built duplicate metadata.
- Keep complex validation and command behavior in Java code, not annotation
  attributes.
- Use Glazed Lists interop as a first-class row source when existing tables
  already depend on `EventList`.
- Read generated source during code review. It should be direct, deterministic,
  and easy to debug.

See [Generated Code Examples](generated-code-examples.md) for the example
source records, generated companion classes, and review checklist.

## Current Limits

- No mutable JavaBean support.
- No runtime reflection adapter in core modules.
- No expression-language validation rules.
- Combo-box option metadata is generated only for enum-valued fields, using
  enum declaration order. Non-enum option sources remain application-owned.
- Generated action classes include descriptor metadata and command factories for
  annotated no-argument owner methods.
- Generated class names, annotation members, generated key ids, and generated
  descriptor shapes are part of the preview compatibility surface described in
  [Release Policy](release-policy.md).
