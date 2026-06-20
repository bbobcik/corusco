package cz.auderis.corusco.test;

import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Small javac harness for tests that inspect annotation-processor output.
 *
 * <p>The harness writes caller-provided sample sources under a temporary work
 * directory, invokes the system javac with configured processors, and returns a
 * {@link GeneratedSourceCompilation} that exposes generated sources and
 * diagnostics. It is intended for unit tests of generated-code contracts, not
 * for production compilation or build orchestration.</p>
 *
 * <p>Instances are mutable builders: processor, release, and classpath settings
 * affect subsequent compilations. Each {@code compile} call receives an
 * isolated output directory under the harness work directory. The harness
 * requires a JDK because it uses {@link ToolProvider#getSystemJavaCompiler()}.</p>
 */
public final class GeneratedSourceCompiler {

    private static final String DEFAULT_RELEASE = "25";

    private final Path workDir;
    private final List<Processor> processors;
    private String release;
    private String classpath;
    private int compilationIndex;

    private GeneratedSourceCompiler(Path workDir) {
        this.workDir = Objects.requireNonNull(workDir, "workDir");
        this.processors = new ArrayList<>();
        this.release = DEFAULT_RELEASE;
        this.classpath = System.getProperty("java.class.path");
    }

    /**
     * Creates a compiler harness rooted in a caller-owned temporary directory.
     *
     * @param workDir temporary work directory, usually provided by JUnit {@code @TempDir}
     * @return compiler harness
     */
    public static GeneratedSourceCompiler in(Path workDir) {
        return new GeneratedSourceCompiler(workDir);
    }

    /**
     * Adds an annotation processor used by subsequent compilations.
     *
     * @param processor annotation processor instance
     * @return this compiler harness
     */
    public GeneratedSourceCompiler withProcessor(Processor processor) {
        processors.add(Objects.requireNonNull(processor, "processor"));
        return this;
    }

    /**
     * Overrides the javac {@code --release} value for subsequent compilations.
     *
     * @param release Java release value
     * @return this compiler harness
     */
    public GeneratedSourceCompiler withRelease(String release) {
        this.release = Objects.requireNonNull(release, "release");
        return this;
    }

    /**
     * Overrides the classpath for subsequent compilations.
     *
     * @param classpath javac classpath value
     * @return this compiler harness
     */
    public GeneratedSourceCompiler withClasspath(String classpath) {
        this.classpath = Objects.requireNonNull(classpath, "classpath");
        return this;
    }

    /**
     * Removes classpath entries whose path contains the supplied text.
     *
     * @param text case-sensitive path fragment
     * @return this compiler harness
     */
    public GeneratedSourceCompiler withoutClasspathEntriesContaining(String text) {
        Objects.requireNonNull(text, "text");
        String[] entries = classpath.split(java.util.regex.Pattern.quote(File.pathSeparator));
        List<String> retained = new ArrayList<>(entries.length);
        for (String entry : entries) {
            if (!entry.contains(text)) {
                retained.add(entry);
            }
        }
        this.classpath = String.join(File.pathSeparator, retained);
        return this;
    }

    /**
     * Compiles a single sample source file.
     *
     * @param sourcePath slash-separated source path, for example {@code demo/CustomerEdit.java}
     * @param source Java source text
     * @return compilation result
     * @throws IOException if source or output directories cannot be created
     */
    public GeneratedSourceCompilation compile(String sourcePath, String source) throws IOException {
        return compile(List.of(new SourceFile(sourcePath, source)));
    }

    /**
     * Compiles one or more sample source files.
     *
     * @param sources sample source files
     * @return compilation result
     * @throws IOException if source or output directories cannot be created
     */
    public GeneratedSourceCompilation compile(List<SourceFile> sources) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("System Java compiler is not available; run tests on a JDK");
        }
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("At least one source file is required");
        }

        Path compilationDir = workDir.resolve("compile-" + (++compilationIndex));
        Path sourceDir = compilationDir.resolve("src");
        Path classesDir = compilationDir.resolve("classes");
        Path generatedSources = compilationDir.resolve("generated");
        Files.createDirectories(classesDir);
        Files.createDirectories(generatedSources);

        List<Path> sourceFiles = new ArrayList<>(sources.size());
        for (SourceFile source : sources) {
            Path sourceFile = sourceDir.resolve(source.path());
            // Tests pass package-shaped paths, so creating parents keeps the
            // physical layout aligned with javac diagnostics and generated code.
            Path parent = sourceFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(sourceFile, source.source(), StandardCharsets.UTF_8);
            sourceFiles.add(sourceFile);
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                diagnostics,
                Locale.ROOT,
                StandardCharsets.UTF_8
        )) {
            Iterable<? extends JavaFileObject> javacSources = fileManager.getJavaFileObjectsFromPaths(sourceFiles);
            List<String> options = List.of(
                    "--release", release,
                    "-classpath", classpath,
                    "-d", classesDir.toString(),
                    "-s", generatedSources.toString()
            );
            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    options,
                    null,
                    javacSources
            );
            task.setProcessors(List.copyOf(processors));

            boolean success = Boolean.TRUE.equals(task.call());
            return new GeneratedSourceCompilation(success, classesDir, generatedSources, messages(diagnostics));
        }
    }

    private static String messages(DiagnosticCollector<JavaFileObject> diagnostics) {
        StringBuilder result = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            result.append(diagnostic.getKind())
                    .append(" line ")
                    .append(diagnostic.getLineNumber())
                    .append(": ")
                    .append(diagnostic.getMessage(Locale.ROOT))
                    .append('\n');
        }
        return result.toString();
    }

    /**
     * Sample source file passed to javac.
     *
     * @param path slash-separated source path
     * @param source Java source text
     */
    public record SourceFile(String path, String source) {

        public SourceFile {
            Objects.requireNonNull(path, "path");
            Objects.requireNonNull(source, "source");
            Path sourcePath = Path.of(path).normalize();
            if (sourcePath.isAbsolute() || sourcePath.startsWith("..")) {
                throw new IllegalArgumentException("Source path must stay under the sample-source root: " + path);
            }
        }
    }
}
