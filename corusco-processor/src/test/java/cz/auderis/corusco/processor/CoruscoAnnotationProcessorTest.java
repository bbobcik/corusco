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
