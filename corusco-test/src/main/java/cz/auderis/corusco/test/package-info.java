/**
 * Test support for compiling sample sources and inspecting Corusco generated
 * Java.
 *
 * <h2>Purpose</h2>
 *
 * <p>This package is for tests that need to verify annotation processor
 * behavior. It wraps the Java compiler API so a test can provide sample source
 * text, run Corusco processors, inspect diagnostics, and read generated source
 * files.</p>
 *
 * <p>Production applications should not depend on this package. It is a test
 * harness for generated-source contracts.</p>
 *
 * <p>The package is useful when a behavior is only visible after annotation
 * processing. A normal unit test can instantiate a class directly; a processor
 * contract test first needs javac to create that class from annotated sample
 * source. This harness owns that compilation step.</p>
 *
 * <h2>Main Types</h2>
 *
 * <p>{@link cz.auderis.corusco.test.GeneratedSourceCompiler} is the entry
 * point. Create it with a temporary directory, add processors, optionally
 * adjust release or classpath settings, and compile one or more sample source
 * files.</p>
 *
 * <p>{@link cz.auderis.corusco.test.GeneratedSourceCompilation} is the result.
 * It exposes whether javac succeeded, compiler diagnostics, the generated
 * source directory, and convenience assertions for checking generated source
 * snippets.</p>
 *
 * <h2>Typical Test</h2>
 *
 * <pre>{@code
 * GeneratedSourceCompilation compilation = GeneratedSourceCompiler.in(tempDir)
 *         .withProcessor(new CoruscoAnnotationProcessor())
 *         .compile("demo/CustomerEdit.java", sourceText);
 *
 * if (!compilation.success()) {
 *     throw new AssertionError(compilation.messages());
 * }
 *
 * compilation.assertGeneratedSourceContains(
 *         "demo/CustomerEditFields.java",
 *         "TextFieldKey"
 * );
 * }</pre>
 *
 * <p>The sample source path should match its package. For example,
 * {@code demo/CustomerEdit.java} is appropriate for {@code package demo;}.
 * The harness writes the source under the temporary compilation directory and
 * asks javac to write generated sources into a separate generated-source
 * directory.</p>
 *
 * <p>Keep sample sources small and focused. If a test checks generated form
 * fields, the sample should include only the field annotations needed for that
 * contract. If a test checks invalid diagnostics, the sample should contain one
 * invalid construct and enough valid surrounding source for javac to reach the
 * processor check.</p>
 *
 * <p>Generated-source assertions should check meaningful contract fragments,
 * not the whole generated file unless the test is deliberately a golden-file
 * review. Small snippet assertions are easier to update when formatting or
 * import ordering changes without changing the generated API.</p>
 *
 * <h2>Why Use This Harness</h2>
 *
 * <p>Generated-code tests are most valuable when they exercise the public
 * contract: annotated Java source goes into javac, generated Java source comes
 * out. This package keeps those tests compact and avoids repeated compiler
 * setup in processor test classes.</p>
 *
 * <p>The harness also produces diagnostics in a predictable text form. When a
 * test fails, the failure can show both the missing generated snippet and the
 * compiler messages that explain invalid sample input.</p>
 *
 * <p>Tests should assert both success and failure paths. A successful sample
 * proves that the processor emits the expected companion class. An invalid
 * sample proves that the processor reports a useful diagnostic instead of
 * generating a broken class or failing with an internal exception.</p>
 *
 * <p>Use generated-source tests for public generated contracts: class names,
 * constants, factory methods, descriptor shape, problem codes, and view
 * interfaces. Internal writer algorithms can have narrower tests, but they
 * should not be the only verification of application-facing generated output.</p>
 *
 * <h2>Boundaries</h2>
 *
 * <p>This package is not a general build tool. It does not replace Gradle,
 * Maven, javac command-line builds, or IDE integration. It is a focused unit
 * test helper for processor and generated-source verification.</p>
 *
 * <p>Swing component testing lives in {@code cz.auderis.corusco.swing.testing}.
 * Use this package when javac and generated source are the subject of the test;
 * use the Swing testing package when component behavior, bindings, or EDT
 * behavior are the subject.</p>
 *
 * <p>Core runtime tests usually do not need this package either. A form model,
 * command, validator, observable value, or table descriptor can normally be
 * tested by constructing it directly. Use this compiler harness when the
 * generated Java source itself is part of what the test must verify.</p>
 *
 * <h2>Common Failure Modes</h2>
 *
 * <p>If {@code success()} is false, read {@code messages()} before inspecting
 * generated files. The sample may have a plain Java syntax error, a missing
 * import, or an intentionally invalid annotation combination.</p>
 *
 * <p>If compilation succeeds but a generated file is missing, verify that the
 * expected annotation is present in the sample and that the appropriate
 * processor was registered with {@code withProcessor(...)}. The harness does
 * not discover processors from build configuration automatically.</p>
 *
 * <p>If a path passed to {@code generatedSource(...)} cannot be read, check the
 * generated package name and class name. Generated files are written under the
 * generated-source root using Java package directory layout.</p>
 *
 * <h2>Requirements</h2>
 *
 * <p>The harness requires a JDK with the system compiler available. A runtime
 * without {@code ToolProvider.getSystemJavaCompiler()} cannot compile sample
 * sources.</p>
 *
 * <p>The caller owns the temporary directory and cleanup. In JUnit tests, this
 * is usually a {@code @TempDir}. Each compile call creates an isolated
 * subdirectory so one test can run several independent compilations.</p>
 */
package cz.auderis.corusco.test;
