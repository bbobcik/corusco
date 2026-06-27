package cz.auderis.corusco.processor;

import java.nio.file.Path;
import java.util.List;
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
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;
                import java.math.BigDecimal;
                import java.time.LocalDate;

                @CoruscoForm(id = "customer")
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
                "EditorDescriptor.text()",
                "CustomerEditResources.NAME_LABEL",
                "CustomerEditResources.NAME_TOOLTIP",
                "HelpTopic.of(\"customer/name\")",
                "ConstraintDescriptor.required(CustomerEditProblems.NAME_REQUIRED)",
                "ConstraintDescriptor.length(CustomerEditProblems.NAME_LENGTH, 0, 80)",
                "ConstraintDescriptor.regex(CustomerEditProblems.NAME_REGEX, \"[A-Za-z ]+\")",
                "ConstraintDescriptor.decimalRange(CustomerEditProblems.CREDIT_LIMIT_DECIMAL_RANGE, \"0.00\", null)",
                "ConstraintDescriptor.intRange(CustomerEditProblems.AGE_INT_RANGE, 0, 120)",
                "public static final FieldDescriptor<CustomerEdit, java.time.LocalDate> VALID_FROM",
                "EditorDescriptor.date()",
                "public static final FieldDescriptor<CustomerEdit, demo.CustomerType> TYPE",
                "EditorDescriptor.comboBox()",
                "public static final FieldDescriptor<CustomerEdit, java.lang.Boolean> ACTIVE",
                "EditorDescriptor.checkBox()"
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
                "public static void install(CustomerEditView view, CustomerEditPresentationModel model, BehaviorScope scope)",
                "StandardBehaviors.textFieldBinding(model.form().name)",
                "StandardBehaviors.validationTooltip(model.form().name.problemSet())",
                "StandardBehaviors.selectAllOnFocus()",
                "StandardBehaviors.checkBoxBinding(model.form().active)"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditBindings.java",
                "public final class CustomerEditBindings",
                "public static void install(CustomerEditView view, CustomerEditPresentationModel model, BehaviorScope scope)",
                "public static void install(CustomerEditView view, CustomerEditFormModel form, BehaviorScope scope)",
                "CustomerEditBehaviorPlan.install(view, model, scope)",
                "install(view, new CustomerEditPresentationModel(form), scope)"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditOptions.java",
                "public final class CustomerEditOptions",
                "public static final OptionDescriptor<demo.CustomerType> TYPE_RETAIL",
                "OptionKey.of(\"retail\")",
                "TYPE_RESOURCES.label(TYPE_RETAIL_KEY)",
                "public static final List<demo.CustomerType> TYPE",
                "List.of(demo.CustomerType.RETAIL, demo.CustomerType.BUSINESS)"
        );
    }

    @Test
    void generatesRadioGroupDescriptorsAndResourceBackedOptionMetadata() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;
                import cz.auderis.corusco.annotations.form.RadioGroup;
                import cz.auderis.corusco.annotations.form.CoruscoForm;

                @CoruscoForm(id = "customer/security")
                public record CustomerEdit(@RadioGroup AuthenticationMode authenticationMode) {
                }

                enum AuthenticationMode {
                    @CoruscoForm.Option(key = "password", order = 2)
                    PASSWORD,
                    @CoruscoForm.Option(key = "certificate", order = 1)
                    CERTIFICATE,
                    EXTERNAL
                }
                """);

        assertThat(result.success()).as(result.messages()).isTrue();
        result.assertGeneratedSourceContains("demo/CustomerEditDescriptors.java",
                "public static final FieldDescriptor<CustomerEdit, demo.AuthenticationMode> AUTHENTICATION_MODE",
                "EditorDescriptor.radioGroup()"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditView.java",
                "JComponent authenticationModeGroup();"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditBehaviorPlan.java",
                "StandardBehaviors.radioGroupBinding(model.form().authenticationMode, "
                        + "CustomerEditOptions.AUTHENTICATION_MODE_DESCRIPTORS)"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditOptions.java",
                "private static final OptionResourcePrefix AUTHENTICATION_MODE_RESOURCES",
                "OptionResourcePrefix.of(CustomerEditFields.AUTHENTICATION_MODE)",
                "public static final OptionKey AUTHENTICATION_MODE_CERTIFICATE_KEY",
                "OptionKey.of(\"certificate\")",
                "public static final OptionDescriptor<demo.AuthenticationMode> AUTHENTICATION_MODE_CERTIFICATE",
                "AUTHENTICATION_MODE_RESOURCES.label(AUTHENTICATION_MODE_CERTIFICATE_KEY)",
                "AUTHENTICATION_MODE_RESOURCES.help(AUTHENTICATION_MODE_CERTIFICATE_KEY)",
                "public static final OptionKey AUTHENTICATION_MODE_EXTERNAL_KEY",
                "OptionKey.of(\"external\")",
                "public static final List<OptionDescriptor<demo.AuthenticationMode>> AUTHENTICATION_MODE_DESCRIPTORS",
                "List.of(AUTHENTICATION_MODE_CERTIFICATE, AUTHENTICATION_MODE_PASSWORD, AUTHENTICATION_MODE_EXTERNAL)",
                "public static final List<demo.AuthenticationMode> AUTHENTICATION_MODE",
                "List.of(demo.AuthenticationMode.CERTIFICATE, demo.AuthenticationMode.PASSWORD, demo.AuthenticationMode.EXTERNAL)"
        );
    }

    @Test
    void generatesImmutableResultImplementationForAnnotatedAbstractClass() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CheckBox;
                import cz.auderis.corusco.annotations.form.ComboBox;
                import cz.auderis.corusco.annotations.validation.Required;
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer")
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
                "public final class GeneratedCustomerEdit",
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
                "if (!(obj instanceof GeneratedCustomerEdit other))",
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
                "public final class CustomerEditFormModel extends AbstractFormModel<GeneratedCustomerEdit>",
                "public CustomerEditFormModel(CustomerEdit original)",
                "original.name()",
                "original.type()",
                "original.active()",
                "protected GeneratedCustomerEdit createResult()",
                "return new GeneratedCustomerEdit(",
                "name.value()",
                "type.value().value()",
                "active.value().value()"
        );
    }

    @Test
    void generatesComponentStateModelsForFieldsAndAuxiliaryStateAccessors() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.ComboBox;
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;
                import cz.auderis.corusco.core.form.ComponentStateModel;

                @CoruscoForm(id = "customer/security")
                public abstract class CustomerEdit {
                    @ComboBox
                    public abstract AuthenticationMode authenticationMode();

                    @TextField
                    @CoruscoForm.ComponentState
                    @CoruscoForm.DependsOn(field = "authenticationMode", values = "PASSWORD", effect = CoruscoForm.DependencyEffect.VISIBLE)
                    public abstract String password();

                    @CoruscoForm.ComponentState
                    public abstract ComponentStateModel advancedSection();
                }

                enum AuthenticationMode {
                    PASSWORD, CERTIFICATE
                }
                """);

        assertThat(result.success()).as(result.messages()).isTrue();
        result.assertGeneratedSourceContains("demo/CustomerEditFormModel.java",
                "return new GeneratedCustomerEdit(",
                "authenticationMode.value().value()",
                "password.value()"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditPresentationModel.java",
                "import cz.auderis.corusco.core.form.ComponentStateModel;",
                "public final class CustomerEditPresentationModel",
                "private final CustomerEditFormModel form;",
                "public final ComponentStateModel passwordState;",
                "public final ComponentStateModel advancedSection;",
                "public CustomerEditPresentationModel(CustomerEditFormModel form)",
                "this.form = Objects.requireNonNull(form, \"form\");",
                "this.passwordState = new ComponentStateModel();",
                "this.advancedSection = new ComponentStateModel();",
                "public CustomerEditFormModel form()",
                "public ComponentStateModel passwordState()",
                "return passwordState;",
                "public ComponentStateModel advancedSection()",
                "return advancedSection;"
        );
        result.assertGeneratedSourceContains("demo/GeneratedCustomerEdit.java",
                "private final demo.AuthenticationMode authenticationMode;",
                "private final java.lang.String password;",
                "public demo.AuthenticationMode authenticationMode()",
                "public java.lang.String password()"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditDependencies.java",
                "public final class CustomerEditDependencies",
                "public static final FieldDependency<?> PASSWORD_STATE_DEPENDS_ON_AUTHENTICATION_MODE",
                "FieldDependency.of(",
                "CustomerEditFields.AUTHENTICATION_MODE,",
                "\"passwordState\"",
                "List.of(demo.AuthenticationMode.PASSWORD)",
                "DependencyEffect.VISIBLE",
                "public static List<FieldDependency<?>> all()",
                "List.of(",
                "PASSWORD_STATE_DEPENDS_ON_AUTHENTICATION_MODE"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditBehaviorPlan.java",
                "StandardBehaviors.componentState(model.passwordState)",
                "scope.add(dependencyBinding(",
                "model.form().authenticationMode.value()",
                "model.passwordState",
                "List.of(demo.AuthenticationMode.PASSWORD)",
                "DependencyEffect.VISIBLE",
                "private static Binding dependencyBinding(",
                "List<?> expectedValues",
                "boolean active = expectedValues.contains(value)",
                "case VISIBLE -> target.visible().setValue(active, StandardChangeOrigin.GENERATED)"
        );
        assertThat(result.generatedSource("demo/GeneratedCustomerEdit.java"))
                .doesNotContain("ComponentStateModel passwordState")
                .doesNotContain("ComponentStateModel advancedSection")
                .doesNotContain("new ComponentStateModel()")
                .doesNotContain("UnsupportedOperationException");
        assertThat(result.generatedSource("demo/CustomerEditFormModel.java"))
                .doesNotContain("ComponentStateModel passwordState")
                .doesNotContain("ComponentStateModel advancedSection");
    }

    @Test
    void dependencyValuesResolveEnumOptionKeysAndBooleanLiterals() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CheckBox;
                import cz.auderis.corusco.annotations.form.ComboBox;
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer/security")
                public abstract class CustomerEdit {
                    @ComboBox
                    public abstract AuthenticationMode authenticationMode();

                    @CheckBox
                    public abstract boolean active();

                    @TextField
                    @CoruscoForm.ComponentState
                    @CoruscoForm.DependsOn(field = "authenticationMode", values = "password", effect = CoruscoForm.DependencyEffect.VISIBLE)
                    @CoruscoForm.DependsOn(field = "active", values = "true", effect = CoruscoForm.DependencyEffect.ENABLED)
                    public abstract String password();
                }

                enum AuthenticationMode {
                    @CoruscoForm.Option(key = "password")
                    PASSWORD,
                    CERTIFICATE
                }
                """);

        assertThat(result.success()).as(result.messages()).isTrue();
        result.assertGeneratedSourceContains("demo/CustomerEditDependencies.java",
                "List.of(demo.AuthenticationMode.PASSWORD)",
                "List.of(Boolean.TRUE)"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditBehaviorPlan.java",
                "List.of(demo.AuthenticationMode.PASSWORD)",
                "List.of(Boolean.TRUE)",
                "boolean active = expectedValues.contains(value)"
        );
    }

    @Test
    void rejectsUnknownEnumDependencyValue() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.ComboBox;
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer/security")
                public abstract class CustomerEdit {
                    @ComboBox
                    public abstract AuthenticationMode authenticationMode();

                    @TextField
                    @CoruscoForm.ComponentState
                    @CoruscoForm.DependsOn(field = "authenticationMode", values = "PASSWROD")
                    public abstract String password();
                }

                enum AuthenticationMode {
                    PASSWORD, CERTIFICATE
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages())
                .contains("@CoruscoForm.DependsOn value does not match an enum constant or option key for authenticationMode: PASSWROD");
    }

    @Test
    void rejectsInvalidBooleanDependencyValue() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CheckBox;
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer/security")
                public abstract class CustomerEdit {
                    @CheckBox
                    public abstract boolean active();

                    @TextField
                    @CoruscoForm.ComponentState
                    @CoruscoForm.DependsOn(field = "active", values = "yes")
                    public abstract String password();
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages())
                .contains("@CoruscoForm.DependsOn value for checkbox field active must be true or false: yes");
    }

    @Test
    void rejectsAuxiliaryComponentStateAccessorWithWrongReturnType() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer/security")
                public abstract class CustomerEdit {
                    @TextField
                    public abstract String password();

                    @CoruscoForm.ComponentState
                    public abstract String advancedSection();
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@CoruscoForm.ComponentState auxiliary accessors must return ComponentStateModel");
    }

    @Test
    void rejectsStateOnlyRecordComponent() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.core.form.ComponentStateModel;

                @CoruscoForm(id = "customer/security")
                public record CustomerEdit(@CoruscoForm.ComponentState ComponentStateModel advancedSection) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Record component @CoruscoForm.ComponentState must accompany a field kind annotation");
    }

    @Test
    void rejectsDependencyWithoutComponentState() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer/security")
                public abstract class CustomerEdit {
                    @TextField
                    public abstract String authenticationMode();

                    @TextField
                    @CoruscoForm.DependsOn(field = "authenticationMode", values = "PASSWORD")
                    public abstract String password();
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@CoruscoForm.DependsOn requires @CoruscoForm.ComponentState");
    }

    @Test
    void rejectsDependencyOnUnknownField() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer/security")
                public abstract class CustomerEdit {
                    @TextField
                    @CoruscoForm.ComponentState
                    @CoruscoForm.DependsOn(field = "authenticationMode", values = "PASSWORD")
                    public abstract String password();
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages())
                .contains("@CoruscoForm.DependsOn field does not match a generated form field in CustomerEdit: authenticationMode");
    }

    @Test
    void rejectsNonRecordCoruscoForm() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CoruscoForm;

                @CoruscoForm(id = "customer")
                public final class CustomerEdit {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@CoruscoForm classes must be abstract");
    }

    @Test
    void rejectsUnannotatedAbstractAccessorInCoruscoForm() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer")
                public abstract class CustomerEdit {
                    @TextField
                    public abstract String name();

                    public abstract String displayName();
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Abstract @CoruscoForm accessor must have a field kind annotation");
    }

    @Test
    void rejectsConcreteAnnotatedMethodInAbstractCoruscoForm() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer")
                public abstract class CustomerEdit {
                    @TextField
                    public String name() {
                        return "Ada";
                    }
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages())
                .contains("@CoruscoForm field annotations on abstract classes require abstract accessor methods");
    }

    @Test
    void rejectsAbstractAccessorWithParameters() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer")
                public abstract class CustomerEdit {
                    @TextField
                    public abstract String name(String locale);
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Abstract @CoruscoForm accessor must not declare parameters");
    }

    @Test
    void rejectsConflictingComponentAnnotations() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CheckBox;
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer")
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

                import cz.auderis.corusco.annotations.CoruscoForm;
                import cz.auderis.corusco.annotations.TextField;

                @CoruscoForm(id = "customer")
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
                import cz.auderis.corusco.annotations.form.CoruscoForm;

                @CoruscoForm(id = "customer")
                public record CustomerEdit(@CheckBox String active) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@CheckBox requires boolean or java.lang.Boolean component type");
    }

    @Test
    void rejectsGenericCoruscoFormRecordInInitialProcessorStage() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer")
                public record CustomerEdit<T>(@TextField T name) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@CoruscoForm generic source types are not supported by this processor stage");
    }

    @Test
    void rejectsLengthOnNonStringTextField() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.validation.Length;
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;
                import java.math.BigDecimal;

                @CoruscoForm(id = "customer")
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
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;
                import java.math.BigDecimal;

                @CoruscoForm(id = "customer")
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
                import cz.auderis.corusco.annotations.form.CoruscoForm;

                @CoruscoForm(id = "customer")
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

                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer name")
                public record CustomerEdit(@TextField String name) {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages())
                .contains("@CoruscoForm id must contain only letters, digits, dots, underscores, dashes, or slashes");
    }

    @Test
    void rejectsDateFieldOnNonLocalDate() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.form.DateField;
                import cz.auderis.corusco.annotations.form.CoruscoForm;

                @CoruscoForm(id = "customer")
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
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer")
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
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer")
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

                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer")
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
                import cz.auderis.corusco.annotations.table.CoruscoTable;

                @CoruscoTable(id = "customer/search")
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
                "CustomerEditColumns.ORDERS"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditTableBindings.java",
                "public final class CustomerEditTableBindings",
                "public static ObservableTableModel<CustomerEdit> installModel(",
                "ObservableList<CustomerEdit> rows",
                "BindingScope scope",
                "ObservableTableModel<CustomerEdit> tableModel =",
                "ObservableTableModel.of(rows, CustomerEditTableDescriptor.DESCRIPTOR)",
                "public static ObservableTableModel<CustomerEdit> installReadOnlyModel(",
                "ObservableReadableCollection<CustomerEdit> rows",
                "ObservableTableModel.readOnly(rows, CustomerEditTableDescriptor.DESCRIPTOR)",
                "table.setModel(tableModel)",
                "scope.add(tableModel)",
                "public static TableSelectionBinding<CustomerEdit> bindSelection(",
                "WritableValue<Integer> selectedModelRow",
                "WritableValue<CustomerEdit> selectedRow",
                "TableSelectionBinding.bind(table, model, selectedModelRow, selectedRow)"
        );
    }

    @Test
    void omitsCoruscoFormInstallationSourcesWhenPackageIsNotAnnotated() throws Exception {
        GeneratedSourceCompilation result = compileWithoutSwingCompanionPackage("""
                package demo;

                import cz.auderis.corusco.annotations.form.CheckBox;
                import cz.auderis.corusco.annotations.form.CoruscoForm;
                import cz.auderis.corusco.annotations.form.TextField;

                @CoruscoForm(id = "customer")
                public record CustomerEdit(
                        @TextField String name,
                        @CheckBox boolean active
                ) {
                }
                """);

        assertThat(result.success()).as(result.messages()).isTrue();
        result.assertGeneratedSourceContains("demo/CustomerEditFields.java",
                "public final class CustomerEditFields"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditFormModel.java",
                "public final class CustomerEditFormModel extends AbstractFormModel<CustomerEdit>"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditPresentationModel.java",
                "public final class CustomerEditPresentationModel"
        );
        assertThat(result.hasGeneratedSource("demo/CustomerEditView.java")).isFalse();
        assertThat(result.hasGeneratedSource("demo/CustomerEditBehaviorPlan.java")).isFalse();
        assertThat(result.hasGeneratedSource("demo/CustomerEditBindings.java")).isFalse();
    }

    @Test
    void omitsCoruscoTableHelpersWhenPackageIsNotAnnotated() throws Exception {
        GeneratedSourceCompilation result = compileWithoutSwingCompanionPackage("""
                package demo;

                import cz.auderis.corusco.annotations.table.Column;
                import cz.auderis.corusco.annotations.table.CoruscoTable;

                @CoruscoTable(id = "customer/search")
                public record CustomerEdit(
                        @Column String name,
                        @Column int orders
                ) {
                }
                """);

        assertThat(result.success()).as(result.messages()).isTrue();
        result.assertGeneratedSourceContains("demo/CustomerEditColumns.java",
                "public final class CustomerEditColumns"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditTableDescriptor.java",
                "public final class CustomerEditTableDescriptor",
                "public static final cz.auderis.corusco.core.table.TableDescriptor<CustomerEdit> DESCRIPTOR"
        );
        assertThat(result.generatedSource("demo/CustomerEditTableDescriptor.java"))
                .doesNotContain("cz.auderis.corusco.swing");
        assertThat(result.hasGeneratedSource("demo/CustomerEditTableBindings.java")).isFalse();
    }

    @Test
    void generatesExplicitCrossPackageCoruscoFormCompanions() throws Exception {
        GeneratedSourceCompilation result = GeneratedSourceCompiler.in(tempDir)
                .withProcessor(new CoruscoAnnotationProcessor())
                .compile(List.of(
                        new GeneratedSourceCompiler.SourceFile("model/CustomerEdit.java", """
                                package model;

                                import cz.auderis.corusco.annotations.form.CoruscoForm;
                                import cz.auderis.corusco.annotations.form.TextField;

                                @CoruscoForm(id = "customer")
                                public record CustomerEdit(@TextField String name) {
                                }
                                """),
                        new GeneratedSourceCompiler.SourceFile("ui/package-info.java", """
                                @cz.auderis.corusco.annotations.SwingCompanionPackage(
                                        forms = model.CustomerEdit.class
                                )
                                package ui;
                                """)
                ));

        assertThat(result.success()).as(result.messages()).isTrue();
        result.assertGeneratedSourceContains("model/CustomerEditFormModel.java",
                "public final class CustomerEditFormModel extends AbstractFormModel<CustomerEdit>"
        );
        assertThat(result.hasGeneratedSource("model/CustomerEditView.java")).isFalse();
        result.assertGeneratedSourceContains("ui/CustomerEditView.java",
                "package ui;",
                "public interface CustomerEditView"
        );
        result.assertGeneratedSourceContains("ui/CustomerEditBindings.java",
                "public static void install(CustomerEditView view, model.CustomerEditPresentationModel model, BehaviorScope scope)",
                "public static void install(CustomerEditView view, model.CustomerEditFormModel form, BehaviorScope scope)",
                "install(view, new model.CustomerEditPresentationModel(form), scope)"
        );
    }

    @Test
    void generatesExplicitSwingCompanionsForCompiledModelClasses() throws Exception {
        GeneratedSourceCompilation modelCompilation = GeneratedSourceCompiler.in(tempDir)
                .withoutClasspathEntriesContaining("corusco-swing")
                .withProcessor(new CoruscoAnnotationProcessor())
                .compile(List.of(
                        new GeneratedSourceCompiler.SourceFile("model/CustomerEdit.java", """
                                package model;

                                import cz.auderis.corusco.annotations.form.CheckBox;
                                import cz.auderis.corusco.annotations.form.CoruscoForm;
                                import cz.auderis.corusco.annotations.form.TextField;

                                @CoruscoForm(id = "customer")
                                public record CustomerEdit(
                                        @TextField String name,
                                        @CheckBox boolean active
                                ) {
                                }
                                """),
                        new GeneratedSourceCompiler.SourceFile("model/CustomerRow.java", """
                                package model;

                                import cz.auderis.corusco.annotations.table.Column;
                                import cz.auderis.corusco.annotations.table.CoruscoTable;

                                @CoruscoTable(id = "customer/search")
                                public record CustomerRow(
                                        @Column String name,
                                        @Column int orders
                                ) {
                                }
                                """)
                ));

        assertThat(modelCompilation.success()).as(modelCompilation.messages()).isTrue();
        assertThat(modelCompilation.hasGeneratedSource("model/CustomerEditView.java")).isFalse();
        assertThat(modelCompilation.hasGeneratedSource("model/CustomerRowTableBindings.java")).isFalse();

        String adapterClasspath = System.getProperty("java.class.path")
                + java.io.File.pathSeparator
                + modelCompilation.classes();
        GeneratedSourceCompilation adapterCompilation = GeneratedSourceCompiler.in(tempDir)
                .withClasspath(adapterClasspath)
                .withProcessor(new CoruscoAnnotationProcessor())
                .compile("ui/package-info.java", """
                        @cz.auderis.corusco.annotations.SwingCompanionPackage(
                                forms = model.CustomerEdit.class,
                                tables = model.CustomerRow.class
                        )
                        package ui;
                        """);

        assertThat(adapterCompilation.success()).as(adapterCompilation.messages()).isTrue();
        adapterCompilation.assertGeneratedSourceContains("ui/CustomerEditBindings.java",
                "public static void install(CustomerEditView view, model.CustomerEditPresentationModel model, BehaviorScope scope)",
                "public static void install(CustomerEditView view, model.CustomerEditFormModel form, BehaviorScope scope)"
        );
        adapterCompilation.assertGeneratedSourceContains("ui/CustomerRowTableBindings.java",
                "ObservableTableModel<model.CustomerRow> tableModel =",
                "ObservableTableModel.of(rows, model.CustomerRowTableDescriptor.DESCRIPTOR)",
                "TableSelectionBinding<model.CustomerRow> bindSelection"
        );
    }

    @Test
    void rejectsNonRecordCoruscoTable() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.table.CoruscoTable;

                @CoruscoTable(id = "customer/search")
                public final class CustomerEdit {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("@CoruscoTable is supported only on records");
    }

    @Test
    void rejectsDuplicateTableColumnIds() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.table.Column;
                import cz.auderis.corusco.annotations.table.CoruscoTable;

                @CoruscoTable(id = "customer/search")
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
                import cz.auderis.corusco.annotations.table.CoruscoTable;

                @CoruscoTable(id = "customer/search")
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
                import cz.auderis.corusco.annotations.table.CoruscoTable;

                @CoruscoTable(id = "customer/search")
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
                import cz.auderis.corusco.annotations.table.CoruscoTable;

                @CoruscoTable(id = "customer/search")
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
    void legacySwingFormStillGeneratesSamePackageSwingCompanions() throws Exception {
        GeneratedSourceCompilation result = GeneratedSourceCompiler.in(tempDir)
                .withProcessor(new CoruscoAnnotationProcessor())
                .compile("demo/CustomerEdit.java", """
                        package demo;

                        import cz.auderis.corusco.annotations.form.SwingForm;
                        import cz.auderis.corusco.annotations.form.TextField;

                        @SwingForm(id = "customer")
                        public record CustomerEdit(@TextField String name) {
                        }
                        """);

        assertThat(result.success()).as(result.messages()).isTrue();
        result.assertGeneratedSourceContains("demo/CustomerEditFields.java",
                "public final class CustomerEditFields",
                "TextFieldKey.of(\"customer/name\", CustomerEdit.class, java.lang.String.class)"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditView.java",
                "public interface CustomerEditView",
                "JTextField nameField();"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditBindings.java",
                "public static void install(CustomerEditView view, CustomerEditPresentationModel model, BehaviorScope scope)",
                "public static void install(CustomerEditView view, CustomerEditFormModel form, BehaviorScope scope)"
        );
    }

    @Test
    void legacySwingTableStillGeneratesSamePackageSwingBindings() throws Exception {
        GeneratedSourceCompilation result = GeneratedSourceCompiler.in(tempDir)
                .withProcessor(new CoruscoAnnotationProcessor())
                .compile("demo/CustomerEdit.java", """
                        package demo;

                        import cz.auderis.corusco.annotations.table.Column;
                        import cz.auderis.corusco.annotations.table.SwingTable;

                        @SwingTable(id = "customer/search")
                        public record CustomerEdit(@Column String name) {
                        }
                        """);

        assertThat(result.success()).as(result.messages()).isTrue();
        result.assertGeneratedSourceContains("demo/CustomerEditTableDescriptor.java",
                "public final class CustomerEditTableDescriptor",
                "public static final cz.auderis.corusco.core.table.TableDescriptor<CustomerEdit> DESCRIPTOR"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditTableBindings.java",
                "public final class CustomerEditTableBindings",
                "ObservableTableModel.of(rows, CustomerEditTableDescriptor.DESCRIPTOR)"
        );
    }

    @Test
    void rejectsMixedCurrentAndLegacyFormAnnotations() throws Exception {
        GeneratedSourceCompilation result = GeneratedSourceCompiler.in(tempDir)
                .withProcessor(new CoruscoAnnotationProcessor())
                .compile("demo/CustomerEdit.java", """
                        package demo;

                        import cz.auderis.corusco.annotations.form.CoruscoForm;
                        import cz.auderis.corusco.annotations.form.SwingForm;
                        import cz.auderis.corusco.annotations.form.TextField;

                        @CoruscoForm(id = "customer")
                        @SwingForm(id = "customer")
                        public record CustomerEdit(@TextField String name) {
                        }
                        """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Use either @CoruscoForm or @SwingForm, not both");
    }

    @Test
    void rejectsMixedCurrentAndLegacyTableAnnotations() throws Exception {
        GeneratedSourceCompilation result = GeneratedSourceCompiler.in(tempDir)
                .withProcessor(new CoruscoAnnotationProcessor())
                .compile("demo/CustomerEdit.java", """
                        package demo;

                        import cz.auderis.corusco.annotations.table.Column;
                        import cz.auderis.corusco.annotations.table.CoruscoTable;
                        import cz.auderis.corusco.annotations.table.SwingTable;

                        @CoruscoTable(id = "customer/search")
                        @SwingTable(id = "customer/search")
                        public record CustomerEdit(@Column String name) {
                        }
                        """);

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).contains("Use either @CoruscoTable or @SwingTable, not both");
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

    @Test
    void generatesDataSetDescriptorsForFixedSchemaRecord() throws Exception {
        GeneratedSourceCompilation result = compile("""
                package demo;

                import cz.auderis.corusco.annotations.dataset.CoruscoDataSet;
                import cz.auderis.corusco.annotations.dataset.Dimension;
                import cz.auderis.corusco.annotations.dataset.Measure;
                import cz.auderis.corusco.annotations.dataset.AggregationFunction;
                import cz.auderis.corusco.annotations.dataset.MissingPolicy;
                import cz.auderis.corusco.annotations.dataset.QualityColumn;
                import cz.auderis.corusco.annotations.dataset.QualityPolicy;
                import cz.auderis.corusco.annotations.dataset.TimeAxis;

                @CoruscoDataSet(id = "market/quotes")
                record CustomerEdit(
                        @TimeAxis(unit = "millis", monotonic = true) long timestampMillis,
                        @Dimension String symbol,
                        @Dimension String venue,
                        @Measure(unit = "USD", missing = MissingPolicy.NAN, quality = QualityPolicy.FLAGS,
                                aggregations = {AggregationFunction.MIN, AggregationFunction.MAX,
                                        AggregationFunction.AVERAGE}) double bid,
                        @Measure(unit = "shares", aggregations = AggregationFunction.SUM) long volume,
                        @QualityColumn(appliesTo = {"bid", "volume"}) int quality
                ) {
                }
                """);

        assertThat(result.success()).as(result.messages()).isTrue();
        result.assertGeneratedSourceContains("demo/CustomerEditDataColumns.java",
                "public final class CustomerEditDataColumns",
                "public static final DataSetKey<CustomerEdit> DATA_SET",
                "DataSetKey.of(\"market/quotes\", CustomerEdit.class)",
                "public static final DataColumnKey<CustomerEdit, java.lang.Long> TIMESTAMP_MILLIS_KEY",
                "DataColumnKey.of(\"market/quotes/timestamp-millis\", DATA_SET, java.lang.Long.class)",
                "public static final DataColumnDescriptor<CustomerEdit, java.lang.Long> TIMESTAMP_MILLIS",
                "DataColumnRole.TIME_AXIS",
                "DataStorageType.LONG_ARRAY",
                "UnitMetadata.of(\"millis\")",
                "public static final DataColumnDescriptor<CustomerEdit, java.lang.Double> BID",
                "MissingPolicy.NAN",
                "QualityPolicy.FLAGS",
                "java.util.Set.of(AggregationFunction.MIN, AggregationFunction.MAX, AggregationFunction.AVERAGE)",
                "public static final DataColumnDescriptor<CustomerEdit, java.lang.Integer> QUALITY",
                "DataColumnRole.QUALITY"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditDataDescriptor.java",
                "public final class CustomerEditDataDescriptor",
                "public static final DataSetDescriptor<CustomerEdit> DESCRIPTOR",
                "CustomerEditDataColumns.DATA_SET",
                "CustomerEditDataColumns.TIMESTAMP_MILLIS",
                "CustomerEditDataColumns.SYMBOL",
                "CustomerEditDataColumns.BID",
                "CustomerEditDataColumns.QUALITY"
        );
        result.assertGeneratedSourceContains("demo/CustomerEditFrame.java",
                "public final class CustomerEditFrame",
                "private final long[] timestampMillis;",
                "private final java.lang.Object[] symbol;",
                "private final double[] bid;",
                "public long timestampMillis(int row)",
                "public java.lang.String symbol(int row)",
                "return (java.lang.String) this.symbol[row];",
                "public CustomerEdit row(int row)",
                "return new CustomerEdit(",
                "public static final class Builder",
                "public Builder add(CustomerEdit row)",
                "timestampMillis[size] = row.timestampMillis();",
                "symbol[size] = row.symbol();",
                "return new CustomerEditFrame(this);"
        );
    }

    @Test
    void rejectsInvalidDataSetShapes() throws Exception {
        GeneratedSourceCompilation duplicateRoles = compile("""
                package demo;

                import cz.auderis.corusco.annotations.dataset.CoruscoDataSet;
                import cz.auderis.corusco.annotations.dataset.Dimension;
                import cz.auderis.corusco.annotations.dataset.TimeAxis;

                @CoruscoDataSet(id = "market/quotes")
                record CustomerEdit(@TimeAxis @Dimension long timestampMillis) {
                }
                """);

        assertThat(duplicateRoles.success()).isFalse();
        assertThat(duplicateRoles.messages()).contains("Use only one data-set role annotation");

        GeneratedSourceCompilation invalidMissing = compile("""
                package demo;

                import cz.auderis.corusco.annotations.dataset.CoruscoDataSet;
                import cz.auderis.corusco.annotations.dataset.Measure;
                import cz.auderis.corusco.annotations.dataset.MissingPolicy;

                @CoruscoDataSet(id = "market/quotes")
                record CustomerEdit(@Measure(missing = MissingPolicy.NAN) long volume) {
                }
                """);

        assertThat(invalidMissing.success()).isFalse();
        assertThat(invalidMissing.messages()).contains("NAN missing policy requires float or double");
    }

    private GeneratedSourceCompilation compile(String source) throws Exception {
        return GeneratedSourceCompiler.in(tempDir)
                .withProcessor(new CoruscoAnnotationProcessor())
                .compile(List.of(
                        new GeneratedSourceCompiler.SourceFile("demo/package-info.java", """
                                @cz.auderis.corusco.annotations.SwingCompanionPackage
                                package demo;
                                """),
                        new GeneratedSourceCompiler.SourceFile("demo/CustomerEdit.java", source)
                ));
    }

    private GeneratedSourceCompilation compileWithoutSwingCompanionPackage(String source) throws Exception {
        return GeneratedSourceCompiler.in(tempDir)
                .withoutClasspathEntriesContaining("corusco-swing")
                .withProcessor(new CoruscoAnnotationProcessor())
                .compile("demo/CustomerEdit.java", source);
    }
}
