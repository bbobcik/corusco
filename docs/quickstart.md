# Corusco Quickstart

This guide shows the shortest path from an immutable business record to
generated form and table helpers. The snippets are based on compiling examples
under `corusco-examples`.

## 1. Add the Example Dependencies

Application modules usually depend on the runtime modules, the annotations, and
the annotation processor:

```groovy
dependencies {
    implementation project(':corusco-core')
    implementation project(':corusco-glazedlists')
    implementation project(':corusco-swing')
    implementation project(':corusco-annotations')
    annotationProcessor project(':corusco-processor')
}
```

After `./gradlew publishToMavenLocal`, an external consumer can use the same
modules from `mavenLocal()`:

```groovy
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation 'cz.auderis.corusco:corusco-core:0.1.0-preview'
    implementation 'cz.auderis.corusco:corusco-glazedlists:0.1.0-preview'
    implementation 'cz.auderis.corusco:corusco-swing:0.1.0-preview'
    implementation 'cz.auderis.corusco:corusco-annotations:0.1.0-preview'
    annotationProcessor 'cz.auderis.corusco:corusco-processor:0.1.0-preview'
}
```

`corusco-glazedlists` is optional, but it is useful when existing screens
already use mature `EventList` pipelines for sorting, filtering, or live row
updates.

## 2. Describe a Form Record

Annotate an immutable record. Corusco uses these annotations at compile time to
create typed keys, descriptors, problem codes, resource keys, and a form model.

```java
@SwingForm(id = "generated-customer")
record GeneratedCustomerEdit(
        @TextField
        @Required
        @Length(max = 80)
        @Regex("[A-Za-z ]+")
        @Help(topic = "generated-customer/name")
        String name,

        @TextField
        @DecimalRange(min = "0.00")
        BigDecimal creditLimit,

        @TextField
        @IntRange(min = 0, max = 120)
        Integer age,

        @DateField LocalDate validFrom,
        @ComboBox GeneratedCustomerType type,
        @CheckBox boolean active
) {
}
```

The processor writes companion classes such as
`GeneratedCustomerEditFields`, `GeneratedCustomerEditDescriptors`,
`GeneratedCustomerEditResources`, `GeneratedCustomerEditProblems`,
`GeneratedCustomerEditFormModel`, `GeneratedCustomerEditView`, and
`GeneratedCustomerEditBehaviorPlan`.

## 3. Use the Generated Form Model

Generated form models are plain Java classes. Text fields expose raw text and
semantic values separately, so a screen can report invalid user input without
discarding the last valid value.

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

model.name.setRawText("", ChangeOrigin.USER);
boolean blockedByRequiredName = !model.isCommittable();

model.name.setRawText("Bob", ChangeOrigin.USER);
model.creditLimit.setRawText("25.50", ChangeOrigin.USER);
model.age.setRawText("45", ChangeOrigin.USER);
model.active.setValue(false, ChangeOrigin.USER);

GeneratedCustomerEdit committed = model.toResult();
```

See `GeneratedFormModelExample` for the complete compiling scenario and
`GeneratedFormModelExampleTest` for the expected behavior.

## 4. Describe a Table Record

Table annotations generate typed column keys, descriptors, resource keys,
row-updater helpers, table descriptors, and Swing binding helpers.

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
        @Help(
                tooltip = "generated-customer-table/name/help",
                topic = "generated-customer-table/name"
        )
        String name,

        @Column(width = 80, sortable = false) int orders
) {
}
```

The generated descriptor keeps table metadata close to the row type. Swing
screens can install a descriptor-backed model without hand-building column
lists:

```java
BasicEventList<GeneratedCustomerRow> eventList = new BasicEventList<>(new ArrayList<>(List.of(
        new GeneratedCustomerRow("Acme", 2),
        new GeneratedCustomerRow("Globex", 5)
)));
GlazedObservableList<GeneratedCustomerRow> observableRows =
        GlazedListsAdapters.observableList(eventList);

try (BindingScope scope = new BindingScope()) {
    JTable table = new JTable();
    ObservableTableModel<GeneratedCustomerRow> model =
            GeneratedCustomerRowTableBindings.installModel(table, observableRows, scope);

    TableStateController<GeneratedCustomerRow> stateController = scope.add(
            TableStateController.install(table, model, new InMemoryTableStateStore())
    );
    TableHeaderColumnVisibilityMenu<GeneratedCustomerRow> visibilityMenu = scope.add(
            TableHeaderColumnVisibilityMenu.install(table, model, stateController)
    );
    visibilityMenu.createMenu();
}
observableRows.close();
```

See `GeneratedTableColumnsExample` for generated columns, table-state
persistence, column visibility menus, generated row updaters, selection binding,
and Glazed Lists interop in one scenario.

## 5. Test Generated Wiring

Use `SwingMvpTester` for presenter/view tests and the generated-source helpers
for processor tests.

```java
SwingMvpTester<CustomerView, CustomerPresenter> tester = SwingMvpTester.create(
        CustomerView::new,
        CustomerPresenter::new
);

tester.enterText(NAME_FIELD, "Ada")
      .assertCommandEnabled(SAVE_ACTION, true)
      .executeCommand(SAVE_ACTION);
```

For annotation processor tests, `corusco-test` exposes a small javac harness:

```java
GeneratedSourceCompilation result = GeneratedSourceCompiler.in(tempDir)
        .withProcessor(new CoruscoAnnotationProcessor())
        .compile("demo/CustomerEdit.java", source);

result.assertGeneratedSourceContains(
        "demo/CustomerEditFields.java",
        "public final class CustomerEditFields"
);
```

These helpers keep tests focused on generated behavior instead of temporary
directory layout, compiler diagnostics, or platform-specific line endings.

## Next References

- `corusco-examples/src/main/java/cz/auderis/corusco/examples`
- `docs/annotations.md`
- `docs/architecture.md`
- `docs/behaviors.md`
- `docs/forms.md`
- `docs/tables.md`
- `docs/commands.md`
- `docs/dialogs.md`
- `docs/testing.md`
- `docs/generated-code-examples.md`
- `docs/corusco-roadmap.md`
- `docs/implementation-stage-plans/README.md`
