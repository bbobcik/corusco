package cz.auderis.corusco.processor;

import java.nio.file.Path;
import cz.auderis.corusco.test.GeneratedSourceCompilation;
import cz.auderis.corusco.test.GeneratedSourceCompiler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class CoruscoAnnotationProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void generatesTypedFieldKeysForAnnotatedRecord() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CheckBox;
                import cz.auderis.corusco.annotations.form.ComboBox;
                import cz.auderis.corusco.annotations.validation.DecimalRange;
                import cz.auderis.corusco.annotations.form.DateField;
                import cz.auderis.corusco.annotations.help.Help;
                import cz.auderis.corusco.annotations.validation.IntRange;
                import cz.auderis.corusco.annotations.validation.Length;
                import cz.auderis.corusco.annotations.validation.Required;
                import cz.auderis.corusco.annotations.validation.Regex;
                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;
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
        result.assertGeneratedSourceContains("demo/CustomerEditFields.java",
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
        result.assertGeneratedSourceContains("demo/CustomerEditResources.java",
                "ResourceKey.of(\"customer/name/label\", String.class)",
                "ResourceKey.of(\"customer/name/tooltip\", String.class)",
                "ResourceKey.of(\"customer/credit-limit/label\", String.class)"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditProblems.java",
                "ProblemCode.of(\"customer/name/required\")",
                "ProblemCode.of(\"customer/name/length\")",
                "ProblemCode.of(\"customer/name/regex\")",
                "ProblemCode.of(\"customer/credit-limit/decimal-range\")",
                "ProblemCode.of(\"customer/age/int-range\")"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditDescriptors.java",
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
        result.assertGeneratedSourceContains("demo/CustomerEditFormModel.java",
                "public final class CustomerEditFormModel extends AbstractFormModel<CustomerEdit>",
                "public final TextFieldModel<CustomerEdit, java.lang.String> name;",
                "public final TextFieldModel<CustomerEdit, java.math.BigDecimal> creditLimit;",
                "public final FieldModel<CustomerEdit, demo.CustomerType> type;",
                """
                            public CustomerEditFormModel(CustomerEdit original) {
                                java.util.Objects.requireNonNull(original, "original");
                                this.name = register(new TextFieldModel<>(
                                        CustomerEditFields.NAME,
                                        original.name(),
                                        Converters.string()
                                ));
                        """,
                "Converters.string()",
                "Converters.bigDecimal(EmptyTextPolicy.NULL_VALUE)",
                "Converters.localDate(EmptyTextPolicy.NULL_VALUE)",
                "rules.field(CustomerEditFields.NAME.asFieldKey(), model -> model.name, Validators.required(\"customer/name/required\"));",
                "rules.field(CustomerEditFields.AGE.asFieldKey(), model -> model.age, Validators.integerRange(0, 120, \"customer/age/int-range\"));",
                "return new CustomerEdit(",
                "name.value()",
                "active.value().value()"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditView.java",
                "public interface CustomerEditView",
                "JTextField nameField();",
                "JTextField validFromField();",
                "JComboBox<demo.CustomerType> typeCombo();",
                "JCheckBox activeBox();"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditBehaviorPlan.java",
                "public final class CustomerEditBehaviorPlan",
                "public static void install(CustomerEditView view, CustomerEditFormModel model, BehaviorScope scope)",
                "StandardBehaviors.textFieldBinding(model.name)",
                "StandardBehaviors.validationTooltip(model.name.problemSet())",
                "StandardBehaviors.selectAllOnFocus()",
                "StandardBehaviors.checkBoxBinding(model.active)"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditBindings.java",
                "public final class CustomerEditBindings",
                "public static void install(CustomerEditView view, CustomerEditFormModel model, BehaviorScope scope)",
                "CustomerEditBehaviorPlan.install(view, model, scope)"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditOptions.java",
                "public final class CustomerEditOptions",
                "public static final List<demo.CustomerType> TYPE",
                "List.of(demo.CustomerType.RETAIL, demo.CustomerType.BUSINESS)"
        );
    }

    @Test
    void generatesImmutableResultImplementationForAnnotatedAbstractClass() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CheckBox;
                import cz.auderis.corusco.annotations.form.ComboBox;
                import cz.auderis.corusco.annotations.validation.Required;
                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @SwingForm(id = "customer")
                public abstract class CustomerEdit {
                    @TextField
                    @Required
                    public abstract String name();

                    @ComboBox
                    public abstract CustomerType type();

                    @CheckBox
                    public abstract boolean active();

                    public boolean corporateCustomer() {
                        return type() == CustomerType.BUSINESS;
                    }
                }

                enum CustomerType {
                    RETAIL, BUSINESS
                }
                """);

        assertThat(result.success()).as(result.messages()).isTrue();
        result.assertGeneratedSourceContains("demo/GeneratedCustomerEdit.java",
                "public final class GeneratedCustomerEdit extends CustomerEdit",
                "private final java.lang.String name;",
                "private final demo.CustomerType type;",
                "private final boolean active;",
                "public GeneratedCustomerEdit(",
                "java.lang.String name,",
                "demo.CustomerType type,",
                "boolean active",
                "public java.lang.String name()",
                "public demo.CustomerType type()",
                "public boolean active()",
                "if (!(obj instanceof CustomerEdit other))",
                "Objects.equals(name(), other.name())",
                "Objects.hash(name(), type(), active())",
                "GeneratedCustomerEdit[",
                "name=\" + name()"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditFields.java",
                "public static final TextFieldKey<CustomerEdit, java.lang.String> NAME",
                "TextFieldKey.of(\"customer/name\", CustomerEdit.class, java.lang.String.class)",
                "public static final FieldKey<CustomerEdit, demo.CustomerType> TYPE",
                "public static final FieldKey<CustomerEdit, java.lang.Boolean> ACTIVE"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditDescriptors.java",
                "public static final FieldDescriptor<CustomerEdit, java.lang.String> NAME",
                "public static final FieldDescriptor<CustomerEdit, demo.CustomerType> TYPE",
                "public static final FieldDescriptor<CustomerEdit, java.lang.Boolean> ACTIVE"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditFormModel.java",
                "public final class CustomerEditFormModel extends AbstractFormModel<CustomerEdit>",
                "public CustomerEditFormModel(CustomerEdit original)",
                "original.name()",
                "original.type()",
                "original.active()",
                "protected CustomerEdit createResult()",
                "return new GeneratedCustomerEdit(",
                "name.value()",
                "type.value().value()",
                "active.value().value()"
        );
    }

    @Test
    void rejectsNonRecordSwingForm() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.SwingForm;

                @SwingForm(id = "customer")
                public final class CustomerEdit {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@SwingForm classes must be abstract");
    }

    @Test
    void rejectsUnannotatedAbstractAccessorInSwingForm() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @SwingForm(id = "customer")
                public abstract class CustomerEdit {
                    @TextField
                    public abstract String name();

                    public abstract String displayName();
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Abstract @SwingForm accessor must have a field kind annotation");
    }

    @Test
    void rejectsConcreteAnnotatedMethodInAbstractSwingForm() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @SwingForm(id = "customer")
                public abstract class CustomerEdit {
                    @TextField
                    public String name() {
                        return "Ada";
                    }
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages())
                .contains("@SwingForm field annotations on abstract classes require abstract accessor methods");
    }

    @Test
    void rejectsAbstractAccessorWithParameters() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @SwingForm(id = "customer")
                public abstract class CustomerEdit {
                    @TextField
                    public abstract String name(String locale);
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Abstract @SwingForm accessor must not declare parameters");
    }

    @Test
    void rejectsConflictingComponentAnnotations() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CheckBox;
                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @SwingForm(id = "customer")
                public record CustomerEdit(@TextField @CheckBox boolean active) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Record component must have only one field kind annotation");
    }

    @Test
    void rejectsOldRootAnnotationImports() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.SwingForm;
                import cz.auderis.corusco.annotations.TextField;

                @SwingForm(id = "customer")
                public record CustomerEdit(@TextField String name) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("cannot find symbol");
    }

    @Test
    void rejectsNonBooleanCheckbox() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CheckBox;
                import cz.auderis.corusco.annotations.form.SwingForm;

                @SwingForm(id = "customer")
                public record CustomerEdit(@CheckBox String active) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@CheckBox requires boolean or java.lang.Boolean component type");
    }

    @Test
    void rejectsGenericSwingFormRecordInInitialProcessorStage() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @SwingForm(id = "customer")
                public record CustomerEdit<T>(@TextField T name) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@SwingForm generic source types are not supported by this processor stage");
    }

    @Test
    void rejectsLengthOnNonStringTextField() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.validation.Length;
                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;
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
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.validation.DecimalRange;
                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;
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
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.validation.Required;
                import cz.auderis.corusco.annotations.form.SwingForm;

                @SwingForm(id = "customer")
                public record CustomerEdit(@Required String name) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Field metadata annotations require a field kind annotation");
    }

    @Test
    void rejectsUnstableGeneratedIds() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;

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
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.DateField;
                import cz.auderis.corusco.annotations.form.SwingForm;

                @SwingForm(id = "customer")
                public record CustomerEdit(@DateField String validFrom) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@DateField requires java.time.LocalDate component type");
    }

    @Test
    void rejectsRegexOnNonStringTextField() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.validation.Regex;
                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @SwingForm(id = "customer")
                public record CustomerEdit(@TextField @Regex("[0-9]+") Integer age) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@Regex is supported only on @TextField String components");
    }

    @Test
    void rejectsInvalidIntRange() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.validation.IntRange;
                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @SwingForm(id = "customer")
                public record CustomerEdit(@TextField @IntRange(min = 10, max = 1) Integer age) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@IntRange requires min <= max");
    }

    @Test
    void rejectsUnsupportedTextFieldTypeForGeneratedFormModel() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.SwingForm;
                import cz.auderis.corusco.annotations.form.TextField;

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
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.table.Column;
                import cz.auderis.corusco.annotations.help.Help;
                import cz.auderis.corusco.annotations.table.SwingTable;

                @SwingTable(id = "customer/search")
                public record CustomerEdit(
                        @Column(
                                persistenceId = "customer/search/main-name",
                                width = 180,
                                minWidth = 80,
                                maxWidth = 320,
                                editable = true
                        )
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

        assertThat(result.success()).as(result.messages()).isTrue();
        result.assertGeneratedSourceContains("demo/CustomerEditColumns.java",
                "public final class CustomerEditColumns",
                "public static final TableKey<CustomerEdit> TABLE",
                "TableKey.of(\"customer/search\", CustomerEdit.class)",
                "public static final ColumnKey<CustomerEdit, java.lang.String> NAME_KEY",
                "ColumnKey.of(\"customer/search/name\", CustomerEdit.class, java.lang.String.class)",
                "CustomerEditTableResources.NAME_HEADER",
                "CustomerEditTableResources.NAME_TOOLTIP",
                "HelpTopic.of(\"customer/search/name\")",
                "ColumnPersistence.of(\"customer/search/main-name\", 80, 320)",
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
                "ColumnPersistence.of(\"customer/search/orders\", 24, Integer.MAX_VALUE)",
                "new ColumnDefaults(80, 3, true)",
                "new ColumnCapabilities(false, false, false, false)",
                "Column.readOnly(ORDERS_DESCRIPTOR, CustomerEdit::orders)"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditTableResources.java",
                "public final class CustomerEditTableResources",
                "ResourceKey.of(\"customer/search/name/header\", String.class)",
                "ResourceKey.of(\"customer/search/name/help\", String.class)",
                "ResourceKey.of(\"customer/search/orders/title\", String.class)"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditTableDescriptor.java",
                "public final class CustomerEditTableDescriptor",
                "public static final cz.auderis.corusco.core.table.TableDescriptor<CustomerEdit> DESCRIPTOR",
                "CustomerEditColumns.TABLE",
                "List.of(",
                "CustomerEditColumns.NAME",
                "CustomerEditColumns.ORDERS",
                "public static ObservableTableModel<CustomerEdit> tableModel(ObservableList<CustomerEdit> rows)",
                "public static ObservableTableModel<CustomerEdit> readOnlyTableModel(",
                "ObservableReadableCollection<CustomerEdit> rows",
                "return ObservableTableModel.readOnly(rows, DESCRIPTOR)"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditTableBindings.java",
                "public final class CustomerEditTableBindings",
                "public static ObservableTableModel<CustomerEdit> installModel(",
                "ObservableList<CustomerEdit> rows",
                "BindingScope scope",
                "ObservableTableModel<CustomerEdit> model = CustomerEditTableDescriptor.tableModel(rows)",
                "public static ObservableTableModel<CustomerEdit> installReadOnlyModel(",
                "ObservableReadableCollection<CustomerEdit> rows",
                "ObservableTableModel<CustomerEdit> model = CustomerEditTableDescriptor.readOnlyTableModel(rows)",
                "table.setModel(model)",
                "scope.add(model)",
                "public static TableSelectionBinding<CustomerEdit> bindSelection(",
                "WritableValue<Integer> selectedModelRow",
                "WritableValue<CustomerEdit> selectedRow",
                "TableSelectionBinding.bind(table, model, selectedModelRow, selectedRow)"
        );
    }

    @Test
    void rejectsNonRecordSwingTable() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.table.SwingTable;

                @SwingTable(id = "customer/search")
                public final class CustomerEdit {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@SwingTable is supported only on records");
    }

    @Test
    void rejectsDuplicateTableColumnIds() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.table.Column;
                import cz.auderis.corusco.annotations.table.SwingTable;

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
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.table.Column;
                import cz.auderis.corusco.annotations.table.SwingTable;

                @SwingTable(id = "customer/search")
                public record CustomerEdit(@Column(width = 0) String name) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@Column width must be greater than zero");
    }

    @Test
    void rejectsInvalidTableColumnPersistenceMetadata() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.table.Column;
                import cz.auderis.corusco.annotations.table.SwingTable;

                @SwingTable(id = "customer/search")
                public record CustomerEdit(@Column(width = 80, minWidth = 100) String name) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@Column requires minWidth <= width");
    }

    @Test
    void rejectsConflictingTableColumnTooltipDeclarations() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.table.Column;
                import cz.auderis.corusco.annotations.help.Help;
                import cz.auderis.corusco.annotations.table.SwingTable;

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
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.command.UiAction;

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
        result.assertGeneratedSourceContains("demo/CustomerEditActions.java",
                "public final class CustomerEditActions",
                "public static final ActionKey SAVE_KEY",
                "ActionKey.of(\"customer/save\")",
                "ResourceKey.of(\"customer/save/text\", String.class)",
                "ResourceKey.of(\"customer/save/tooltip\", String.class)",
                "ActionDescriptor.action(SAVE_KEY, SAVE_TEXT).withTooltip(SAVE_TOOLTIP).withMnemonic(83)"
                        + ".withAccelerator(AcceleratorDescriptor.of(83, 128))",
                "public static final ActionKey TOGGLE_ACTIVE_KEY",
                "ActionDescriptor.toggle(TOGGLE_ACTIVE_KEY, TOGGLE_ACTIVE_TEXT)",
                "public static List<ActionDescriptor> descriptors()",
                "public static List<ActionDescriptor> menuDescriptors()",
                "public static List<ActionDescriptor> toolbarDescriptors()",
                "public static MutableCommand saveCommand(CustomerEdit owner)",
                "CommandFactory.command(SAVE, command -> owner.save())",
                "public static MutableCommand toggleActiveCommand(CustomerEdit owner)",
                "CommandFactory.toggle(TOGGLE_ACTIVE, false, command -> owner.toggleActive())",
                "public static CommandSet commands(CustomerEdit owner)",
                "saveCommand(owner)",
                "toggleActiveCommand(owner)"
        );
    }

    @Test
    void rejectsUiActionMethodsWithParameters() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.command.UiAction;

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
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.command.UiAction;

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

    private GeneratedSourceCompilation compile(String source) throws Exception {
        return GeneratedSourceCompiler.in(tempDir)
                .withProcessor(new CoruscoAnnotationProcessor())
                .compile("demo/CustomerEdit.java", source);
    }
}
