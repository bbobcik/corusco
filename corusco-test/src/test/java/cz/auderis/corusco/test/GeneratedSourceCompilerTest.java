package cz.auderis.corusco.test;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneratedSourceCompilerTest {

    @TempDir
    Path tempDir;

    @Test
    void compilesSampleSourceAndAssertsGeneratedSource() throws Exception {
        GeneratedSourceCompiler compiler = GeneratedSourceCompiler.in(tempDir)
                .withProcessor(new DemoProcessor());

        GeneratedSourceCompilation result = compiler.compile("demo/Input.java", """
                package demo;

                final class Input {
                }
                """);

        assertThat(result.success()).isTrue();
        // The assertion helper reads from javac's generated-source output and
        // normalizes line endings so tests stay portable across developer OSes.
        result.assertGeneratedSourceContains("demo/GeneratedByTest.java",
                "package demo;",
                "final class GeneratedByTest"
        );
    }

    @Test
    void reportsMissingGeneratedSourceSnippetWithDiagnostics() throws Exception {
        GeneratedSourceCompilation result = GeneratedSourceCompiler.in(tempDir)
                .withProcessor(new DemoProcessor())
                .compile("demo/Input.java", """
                        package demo;

                        final class Input {
                        }
                        """);

        // The failure includes the generated file and diagnostics, which makes
        // processor regressions easier to diagnose from CI logs.
        assertThatThrownBy(() -> result.assertGeneratedSourceContains(
                "demo/GeneratedByTest.java",
                "missing()"
        ))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("demo/GeneratedByTest.java")
                .hasMessageContaining("missing()");
    }

    private static final class DemoProcessor extends AbstractProcessor {

        private boolean generated;

        @Override
        public Set<String> getSupportedAnnotationTypes() {
            return Set.of("*");
        }

        @Override
        public SourceVersion getSupportedSourceVersion() {
            return SourceVersion.latest();
        }

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            if (roundEnv.processingOver() || generated) {
                return false;
            }
            try {
                generated = true;
                JavaFileObject file = processingEnv.getFiler().createSourceFile("demo.GeneratedByTest");
                try (Writer writer = file.openWriter()) {
                    writer.write("""
                            package demo;

                            final class GeneratedByTest {
                            }
                            """);
                }
                return false;
            } catch (IOException e) {
                throw new IllegalStateException("Cannot generate demo source", e);
            }
        }
    }
}
