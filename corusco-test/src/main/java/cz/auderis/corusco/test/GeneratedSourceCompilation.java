package cz.auderis.corusco.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Result of compiling sample sources with annotation processors enabled.
 *
 * <p>The result captures the javac success flag, generated-source directory,
 * and diagnostics produced by {@link GeneratedSourceCompiler}. It is intended
 * for tests that inspect generated files directly. The instance does not own or
 * delete the compilation directory; ownership remains with the temporary
 * directory supplied to the compiler harness.</p>
 */
public final class GeneratedSourceCompilation {

    private final boolean success;
    private final Path classes;
    private final Path generatedSources;
    private final String messages;

    GeneratedSourceCompilation(boolean success, Path classes, Path generatedSources, String messages) {
        this.success = success;
        this.classes = Objects.requireNonNull(classes, "classes");
        this.generatedSources = Objects.requireNonNull(generatedSources, "generatedSources");
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    /**
     * Returns whether javac completed successfully.
     *
     * @return {@code true} when compilation succeeded
     */
    public boolean success() {
        return success;
    }

    /**
     * Returns the directory containing compiled class files.
     *
     * @return class-output directory
     */
    public Path classes() {
        return classes;
    }

    /**
     * Returns the directory containing generated source files.
     *
     * @return generated-source output directory
     */
    public Path generatedSources() {
        return generatedSources;
    }

    /**
     * Returns collected compiler diagnostics as a single readable string.
     *
     * @return compiler diagnostics
     */
    public String messages() {
        return messages;
    }

    /**
     * Reads one generated source file and normalizes line endings.
     *
     * <p>The path is resolved under {@link #generatedSources()} and must remain
     * relative. Windows and old-Mac line endings are normalized to {@code \n} to
     * keep textual assertions platform-independent.</p>
     *
     * @param relativePath slash-separated path under the generated-source root
     * @return generated source text
     * @throws IOException if the generated source cannot be read
     */
    public String generatedSource(String relativePath) throws IOException {
        Path source = generatedSources.resolve(relativePath(relativePath));
        String text = Files.readString(source, StandardCharsets.UTF_8);
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }

    /**
     * Returns whether a generated source file exists.
     *
     * @param relativePath slash-separated path under the generated-source root
     * @return {@code true} when the source file exists
     */
    public boolean hasGeneratedSource(String relativePath) {
        return Files.exists(generatedSources.resolve(relativePath(relativePath)));
    }

    /**
     * Asserts that a generated source file contains all expected snippets.
     *
     * <p>The assertion message includes the missing snippet and compiler
     * diagnostics so generated-source tests fail with enough context to debug
     * processor changes.</p>
     *
     * @param relativePath slash-separated path under the generated-source root
     * @param snippets expected source snippets
     * @throws IOException if the generated source cannot be read
     */
    public void assertGeneratedSourceContains(String relativePath, String... snippets) throws IOException {
        String source = generatedSource(relativePath);
        for (String snippet : snippets) {
            if (!source.contains(snippet)) {
                throw new AssertionError("""
                        Expected generated source to contain snippet.
                        Source: %s
                        Missing snippet:
                        %s
                        Compiler diagnostics:
                        %s
                        """.formatted(relativePath, snippet, messages));
            }
        }
    }

    private static Path relativePath(String path) {
        Path relativePath = Path.of(Objects.requireNonNull(path, "path")).normalize();
        if (relativePath.isAbsolute() || relativePath.startsWith("..")) {
            throw new IllegalArgumentException("Path must stay under the generated-source root: " + path);
        }
        return relativePath;
    }
}
