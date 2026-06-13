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
                import cz.auderis.corusco.annotations.SwingForm;
                import cz.auderis.corusco.annotations.TextField;
                import java.math.BigDecimal;

                @SwingForm(id = "customer")
                public record CustomerEdit(
                        @TextField String name,
                        @TextField BigDecimal creditLimit,
                        @CheckBox boolean active
                ) {
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
                "public static final FieldKey<CustomerEdit, java.lang.Boolean> ACTIVE",
                "FieldKey.of(\"customer/active\", CustomerEdit.class, java.lang.Boolean.class)"
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
        assertThat(result.messages()).contains("Record component cannot be both @TextField and @CheckBox");
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
