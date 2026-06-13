package cz.auderis.corusco.processor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class CoruscoAnnotationProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void generatesTypedFieldKeysForAnnotatedRecord() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.CheckBox;
                import cz.auderis.corusco.annotations.ComboBox;
                import cz.auderis.corusco.annotations.DecimalRange;
                import cz.auderis.corusco.annotations.DateField;
                import cz.auderis.corusco.annotations.Help;
                import cz.auderis.corusco.annotations.IntRange;
                import cz.auderis.corusco.annotations.Length;
                import cz.auderis.corusco.annotations.Required;
                import cz.auderis.corusco.annotations.Regex;
                import cz.auderis.corusco.annotations.SwingForm;
                import cz.auderis.corusco.annotations.TextField;
                import java.math.BigDecimal;
                import java.time.LocalDate;

                @SwingForm(id = "customer")
                public record CustomerEdit(
                        @TextField @Required @Length(max = 80) @Regex("[A-Za-z ]+") @Help(topic = "customer/name") String name,
                        @TextField @DecimalRange(min = "0.00") BigDecimal creditLimit,
                        @TextField @IntRange(min = 0, max = 120) Integer age,
                        @DateField LocalDate validFrom,
                        @ComboBox CustomerType type,
                        @CheckBox boolean active
                ) {
                }

                enum CustomerType {
                    RETAIL, BUSINESS
                }
                """);

        assertThat(result.success()).isTrue();
        String generated = Files.readString(
                result.generatedSources().resolve("demo/CustomerEditFields.java"),
                StandardCharsets.UTF_8
        );
        assertThat(generated).contains(
                "public final class CustomerEditFields",
                "public static final TextFieldKey<CustomerEdit, java.lang.String> NAME",
                "TextFieldKey.of(\"customer/name\", CustomerEdit.class, java.lang.String.class)",
                "public static final TextFieldKey<CustomerEdit, java.math.BigDecimal> CREDIT_LIMIT",
                "TextFieldKey.of(\"customer/credit-limit\", CustomerEdit.class, java.math.BigDecimal.class)",
                "public static final TextFieldKey<CustomerEdit, java.lang.Integer> AGE",
                "TextFieldKey.of(\"customer/age\", CustomerEdit.class, java.lang.Integer.class)",
                "public static final TextFieldKey<CustomerEdit, java.time.LocalDate> VALID_FROM",
                "TextFieldKey.of(\"customer/valid-from\", CustomerEdit.class, java.time.LocalDate.class)",
                "public static final FieldKey<CustomerEdit, demo.CustomerType> TYPE",
                "FieldKey.of(\"customer/type\", CustomerEdit.class, demo.CustomerType.class)",
                "public static final FieldKey<CustomerEdit, java.lang.Boolean> ACTIVE",
                "FieldKey.of(\"customer/active\", CustomerEdit.class, java.lang.Boolean.class)"
        );
        String resources = Files.readString(
                result.generatedSources().resolve("demo/CustomerEditResources.java"),
                StandardCharsets.UTF_8
        );
        assertThat(resources).contains(
                "ResourceKey.of(\"customer/name/label\", String.class)",
                "ResourceKey.of(\"customer/name/tooltip\", String.class)",
                "ResourceKey.of(\"customer/credit-limit/label\", String.class)"
        );
        String problems = Files.readString(
                result.generatedSources().resolve("demo/CustomerEditProblems.java"),
                StandardCharsets.UTF_8
        );
        assertThat(problems).contains(
                "ProblemCode.of(\"customer/name/required\")",
                "ProblemCode.of(\"customer/name/length\")",
                "ProblemCode.of(\"customer/name/regex\")",
                "ProblemCode.of(\"customer/credit-limit/decimal-range\")",
                "ProblemCode.of(\"customer/age/int-range\")"
        );
        String descriptors = Files.readString(
                result.generatedSources().resolve("demo/CustomerEditDescriptors.java"),
                StandardCharsets.UTF_8
        );
        assertThat(descriptors).contains(
                "public static final FieldDescriptor<CustomerEdit, java.lang.String> NAME",
                "\"customer/name\"",
                "\"name\"",
                "FieldKind.TEXT",
                "CustomerEditResources.NAME_LABEL",
                "CustomerEditResources.NAME_TOOLTIP",
                "HelpTopic.of(\"customer/name\")",
                "ConstraintDescriptor.required(CustomerEditProblems.NAME_REQUIRED)",
                "ConstraintDescriptor.length(CustomerEditProblems.NAME_LENGTH, 0, 80)",
                "ConstraintDescriptor.regex(CustomerEditProblems.NAME_REGEX, \"[A-Za-z ]+\")",
                "ConstraintDescriptor.decimalRange(CustomerEditProblems.CREDIT_LIMIT_DECIMAL_RANGE, \"0.00\", null)",
                "ConstraintDescriptor.intRange(CustomerEditProblems.AGE_INT_RANGE, 0, 120)",
                "public static final FieldDescriptor<CustomerEdit, java.time.LocalDate> VALID_FROM",
                "FieldKind.DATE",
                "public static final FieldDescriptor<CustomerEdit, demo.CustomerType> TYPE",
                "FieldKind.COMBO_BOX",
                "public static final FieldDescriptor<CustomerEdit, java.lang.Boolean> ACTIVE",
                "FieldKind.CHECK_BOX"
        );
        String formModel = Files.readString(
                result.generatedSources().resolve("demo/CustomerEditFormModel.java"),
                StandardCharsets.UTF_8
        );
        assertThat(formModel).contains(
                "public final class CustomerEditFormModel extends AbstractFormModel<CustomerEdit>",
                "public final TextFieldModel<CustomerEdit, java.lang.String> name;",
                "public final TextFieldModel<CustomerEdit, java.math.BigDecimal> creditLimit;",
                "public final FieldModel<CustomerEdit, demo.CustomerType> type;",
                "this.name = register(new TextFieldModel<>(",
                "Converters.string()",
                "Converters.bigDecimal(EmptyTextPolicy.NULL_VALUE)",
                "Converters.localDate(EmptyTextPolicy.NULL_VALUE)",
                "rules.field(CustomerEditFields.NAME.asFieldKey(), model -> model.name, Validators.required(\"customer/name/required\"));",
                "rules.field(CustomerEditFields.AGE.asFieldKey(), model -> model.age, Validators.integerRange(0, 120, \"customer/age/int-range\"));",
                "return new CustomerEdit(",
                "name.value()",
                "active.value().value()"
        );
        String view = Files.readString(
                result.generatedSources().resolve("demo/CustomerEditView.java"),
                StandardCharsets.UTF_8
        );
        assertThat(view).contains(
                "public interface CustomerEditView",
                "JTextField nameField();",
                "JTextField validFromField();",
                "JComboBox<demo.CustomerType> typeCombo();",
                "JCheckBox activeBox();"
        );
        String behaviorPlan = Files.readString(
                result.generatedSources().resolve("demo/CustomerEditBehaviorPlan.java"),
                StandardCharsets.UTF_8
        );
        assertThat(behaviorPlan).contains(
                "public final class CustomerEditBehaviorPlan",
                "public static void install(CustomerEditView view, CustomerEditFormModel model, BehaviorScope scope)",
                "StandardBehaviors.textFieldBinding(model.name)",
                "StandardBehaviors.validationTooltip(model.name.problemSet())",
                "StandardBehaviors.selectAllOnFocus()",
                "StandardBehaviors.checkBoxBinding(model.active)"
        );
    }

    @Test
    void rejectsNonRecordSwingForm() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.SwingForm;

                @SwingForm(id = "customer")
                public final class CustomerEdit {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@SwingForm is supported only on records");
    }

    @Test
    void rejectsConflictingComponentAnnotations() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.CheckBox;
                import cz.auderis.corusco.annotations.SwingForm;
                import cz.auderis.corusco.annotations.TextField;

                @SwingForm(id = "customer")
                public record CustomerEdit(@TextField @CheckBox boolean active) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Record component must have only one field kind annotation");
    }

    @Test
    void rejectsNonBooleanCheckbox() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.CheckBox;
                import cz.auderis.corusco.annotations.SwingForm;

                @SwingForm(id = "customer")
                public record CustomerEdit(@CheckBox String active) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@CheckBox requires boolean or java.lang.Boolean component type");
    }

    @Test
    void rejectsGenericSwingFormRecordInInitialProcessorStage() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.SwingForm;
                import cz.auderis.corusco.annotations.TextField;

                @SwingForm(id = "customer")
                public record CustomerEdit<T>(@TextField T name) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@SwingForm generic records are not supported by this processor stage");
    }

    @Test
    void rejectsLengthOnNonStringTextField() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.Length;
                import cz.auderis.corusco.annotations.SwingForm;
                import cz.auderis.corusco.annotations.TextField;
                import java.math.BigDecimal;

                @SwingForm(id = "customer")
                public record CustomerEdit(@TextField @Length(max = 10) BigDecimal creditLimit) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@Length is supported only on @TextField String components");
    }

    @Test
    void rejectsInvalidDecimalRange() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.DecimalRange;
                import cz.auderis.corusco.annotations.SwingForm;
                import cz.auderis.corusco.annotations.TextField;
                import java.math.BigDecimal;

                @SwingForm(id = "customer")
                public record CustomerEdit(@TextField @DecimalRange(min = "10", max = "1") BigDecimal creditLimit) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@DecimalRange requires min <= max");
    }

    @Test
    void rejectsMetadataOnUnannotatedRecordComponent() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.Required;
                import cz.auderis.corusco.annotations.SwingForm;

                @SwingForm(id = "customer")
                public record CustomerEdit(@Required String name) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Field metadata annotations require a field kind annotation");
    }

    @Test
    void rejectsUnstableGeneratedIds() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.SwingForm;
                import cz.auderis.corusco.annotations.TextField;

                @SwingForm(id = "customer name")
                public record CustomerEdit(@TextField String name) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages())
                .contains("@SwingForm id must contain only letters, digits, dots, underscores, dashes, or slashes");
    }

    @Test
    void rejectsDateFieldOnNonLocalDate() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.DateField;
                import cz.auderis.corusco.annotations.SwingForm;

                @SwingForm(id = "customer")
                public record CustomerEdit(@DateField String validFrom) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@DateField requires java.time.LocalDate component type");
    }

    @Test
    void rejectsRegexOnNonStringTextField() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.Regex;
                import cz.auderis.corusco.annotations.SwingForm;
                import cz.auderis.corusco.annotations.TextField;

                @SwingForm(id = "customer")
                public record CustomerEdit(@TextField @Regex("[0-9]+") Integer age) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@Regex is supported only on @TextField String components");
    }

    @Test
    void rejectsInvalidIntRange() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.IntRange;
                import cz.auderis.corusco.annotations.SwingForm;
                import cz.auderis.corusco.annotations.TextField;

                @SwingForm(id = "customer")
                public record CustomerEdit(@TextField @IntRange(min = 10, max = 1) Integer age) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@IntRange requires min <= max");
    }

    @Test
    void rejectsUnsupportedTextFieldTypeForGeneratedFormModel() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.SwingForm;
                import cz.auderis.corusco.annotations.TextField;

                @SwingForm(id = "customer")
                public record CustomerEdit(@TextField Double score) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages())
                .contains("@TextField supports String, Integer, BigDecimal, and LocalDate in this processor stage");
    }

    @Test
    void generatesTableColumnsAndDescriptorForAnnotatedRecord() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.Column;
                import cz.auderis.corusco.annotations.Help;
                import cz.auderis.corusco.annotations.SwingTable;

                @SwingTable(id = "customer/search")
                public record CustomerEdit(
                        @Column(width = 180, editable = true)
                        @Help(tooltip = "customer/search/name/help", topic = "customer/search/name")
                        String name,
                        String ignored,
                        @Column(
                                id = "customer/search/orders",
                                header = "customer/search/orders/title",
                                width = 80,
                                order = 3,
                                sortable = false,
                                filterable = false,
                                hideable = false
                        ) int orders
                ) {
                }
                """);

        assertThat(result.success()).isTrue();
        String columns = Files.readString(
                result.generatedSources().resolve("demo/CustomerEditColumns.java"),
                StandardCharsets.UTF_8
        );
        assertThat(columns).contains(
                "public final class CustomerEditColumns",
                "public static final TableKey<CustomerEdit> TABLE",
                "TableKey.of(\"customer/search\", CustomerEdit.class)",
                "public static final ColumnKey<CustomerEdit, java.lang.String> NAME_KEY",
                "ColumnKey.of(\"customer/search/name\", CustomerEdit.class, java.lang.String.class)",
                "CustomerEditTableResources.NAME_HEADER",
                "CustomerEditTableResources.NAME_TOOLTIP",
                "HelpTopic.of(\"customer/search/name\")",
                "new ColumnDefaults(180, 0, true)",
                "new ColumnCapabilities(true, true, true, true)",
                "Column.editable(NAME_DESCRIPTOR, CustomerEdit::name, CustomerEditColumns::updateName)",
                "private static CustomerEdit updateName(CustomerEdit row, java.lang.String value)",
                "return new CustomerEdit(",
                "value",
                "row.ignored()",
                "row.orders()",
                "public static final ColumnKey<CustomerEdit, java.lang.Integer> ORDERS_KEY",
                "ColumnKey.of(\"customer/search/orders\", CustomerEdit.class, java.lang.Integer.class)",
                "CustomerEditTableResources.ORDERS_HEADER",
                "new ColumnDefaults(80, 3, true)",
                "new ColumnCapabilities(false, false, false, false)",
                "Column.readOnly(ORDERS_DESCRIPTOR, CustomerEdit::orders)"
        );
        String resources = Files.readString(
                result.generatedSources().resolve("demo/CustomerEditTableResources.java"),
                StandardCharsets.UTF_8
        );
        assertThat(resources).contains(
                "public final class CustomerEditTableResources",
                "ResourceKey.of(\"customer/search/name/header\", String.class)",
                "ResourceKey.of(\"customer/search/name/help\", String.class)",
                "ResourceKey.of(\"customer/search/orders/title\", String.class)"
        );
        String descriptor = Files.readString(
                result.generatedSources().resolve("demo/CustomerEditTableDescriptor.java"),
                StandardCharsets.UTF_8
        );
        assertThat(descriptor).contains(
                "public final class CustomerEditTableDescriptor",
                "public static final cz.auderis.corusco.core.table.TableDescriptor<CustomerEdit> DESCRIPTOR",
                "CustomerEditColumns.TABLE",
                "List.of(",
                "CustomerEditColumns.NAME",
                "CustomerEditColumns.ORDERS",
                "public static ObservableTableModel<CustomerEdit> tableModel(ObservableList<CustomerEdit> rows)"
        );
    }

    @Test
    void rejectsNonRecordSwingTable() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.SwingTable;

                @SwingTable(id = "customer/search")
                public final class CustomerEdit {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@SwingTable is supported only on records");
    }

    @Test
    void rejectsDuplicateTableColumnIds() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.Column;
                import cz.auderis.corusco.annotations.SwingTable;

                @SwingTable(id = "customer/search")
                public record CustomerEdit(
                        @Column(id = "customer/search/name") String name,
                        @Column(id = "customer/search/name") String displayName
                ) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Duplicate @Column id in CustomerEdit: customer/search/name");
    }

    @Test
    void rejectsInvalidTableColumnWidth() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.Column;
                import cz.auderis.corusco.annotations.SwingTable;

                @SwingTable(id = "customer/search")
                public record CustomerEdit(@Column(width = 0) String name) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@Column width must be greater than zero");
    }

    @Test
    void rejectsConflictingTableColumnTooltipDeclarations() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.Column;
                import cz.auderis.corusco.annotations.Help;
                import cz.auderis.corusco.annotations.SwingTable;

                @SwingTable(id = "customer/search")
                public record CustomerEdit(
                        @Column(tooltip = "customer/search/name/help")
                        @Help(tooltip = "customer/search/name/other-help")
                        String name
                ) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages())
                .contains("Table column tooltip must be declared either on @Column or @Help, not both");
    }

    @Test
    void generatesActionDescriptorsForUiActionMethods() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.UiAction;

                public final class CustomerEdit {
                    @UiAction(
                            id = "customer/save",
                            text = "customer/save/text",
                            tooltip = "customer/save/tooltip",
                            mnemonic = 83,
                            acceleratorKey = 83,
                            acceleratorModifiers = 128
                    )
                    public void save() {
                    }

                    @UiAction(id = "customer/active", selectable = true)
                    void toggleActive() {
                    }
                }
                """);

        assertThat(result.success()).isTrue();
        String generated = Files.readString(
                result.generatedSources().resolve("demo/CustomerEditActions.java"),
                StandardCharsets.UTF_8
        );
        assertThat(generated).contains(
                "public final class CustomerEditActions",
                "public static final ActionKey SAVE_KEY",
                "ActionKey.of(\"customer/save\")",
                "ResourceKey.of(\"customer/save/text\", String.class)",
                "ResourceKey.of(\"customer/save/tooltip\", String.class)",
                "ActionDescriptor.action(SAVE_KEY, SAVE_TEXT).withTooltip(SAVE_TOOLTIP).withMnemonic(83)"
                        + ".withAccelerator(AcceleratorDescriptor.of(83, 128))",
                "public static final ActionKey TOGGLE_ACTIVE_KEY",
                "ActionDescriptor.toggle(TOGGLE_ACTIVE_KEY, TOGGLE_ACTIVE_TEXT)"
        );
    }

    @Test
    void rejectsUiActionMethodsWithParameters() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.UiAction;

                public final class CustomerEdit {
                    @UiAction(id = "customer/save")
                    void save(String name) {
                    }
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@UiAction methods must not declare parameters");
    }

    @Test
    void rejectsDuplicateUiActionIdsPerOwner() throws Exception {
        CompilationResult result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.UiAction;

                public final class CustomerEdit {
                    @UiAction(id = "customer/save")
                    void save() {
                    }

                    @UiAction(id = "customer/save")
                    void saveAgain() {
                    }
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Duplicate @UiAction id in CustomerEdit: customer/save");
    }

    private CompilationResult compile(String source) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("system Java compiler").isNotNull();
        Path sourceDir = tempDir.resolve("src");
        Path classesDir = tempDir.resolve("classes");
        Path generatedSources = tempDir.resolve("generated");
        Files.createDirectories(sourceDir.resolve("demo"));
        Files.createDirectories(classesDir);
        Files.createDirectories(generatedSources);
        Path sourceFile = sourceDir.resolve("demo/CustomerEdit.java");
        Files.writeString(sourceFile, source, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                diagnostics,
                Locale.ROOT,
                StandardCharsets.UTF_8
        )) {
            Iterable<? extends JavaFileObject> sources = fileManager.getJavaFileObjects(sourceFile);
            List<String> options = List.of(
                    "--release", "25",
                    "-classpath", System.getProperty("java.class.path"),
                    "-d", classesDir.toString(),
                    "-s", generatedSources.toString()
            );
            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    options,
                    null,
                    sources
            );
            task.setProcessors(List.of(new CoruscoAnnotationProcessor()));
            boolean success = Boolean.TRUE.equals(task.call());
            String messages = diagnostics.getDiagnostics().stream()
                    .map(diagnostic -> diagnostic.getMessage(Locale.ROOT))
                    .reduce("", (left, right) -> left + right + "\n");
            return new CompilationResult(success, generatedSources, messages);
        }
    }

    private record CompilationResult(boolean success, Path generatedSources, String messages) {
    }
}
