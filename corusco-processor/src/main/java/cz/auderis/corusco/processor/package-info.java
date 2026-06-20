/**
 * Annotation processor that turns Corusco annotations into generated Java
 * support code.
 *
 * <h2>What The Processor Does</h2>
 *
 * <p>The processor reads source annotations from
 * {@code cz.auderis.corusco.annotations} during compilation and writes ordinary
 * Java source files. Those generated files contain field keys, table keys,
 * column keys, action keys, resource keys, field descriptions, table
 * descriptions, form models, option metadata, dependency metadata, command
 * factories, behavior plans, and binding facades.</p>
 *
 * <p>A field description, table description, or action description is an
 * immutable object that records the stable facts of a screen. The processor
 * writes those objects so runtime code can use ordinary Java constants instead
 * of scanning annotations or retyping strings.</p>
 *
 * <p>Application code normally does not call the processor. The build tool
 * configures it as an annotation processor, javac invokes it, and the generated
 * source is compiled together with the application. The generated classes are
 * then used by normal application code.</p>
 *
 * <p>The public processor class is {@link
 * cz.auderis.corusco.processor.CoruscoAnnotationProcessor}. Build tools and
 * tests may instantiate it directly, but application runtime code should
 * consume generated classes and runtime APIs instead.</p>
 *
 * <h2>Why Compile-Time Generation</h2>
 *
 * <p>Runtime reflection is a poor fit for Corusco's goals. Swing applications
 * benefit from predictable startup, compiler-visible errors, IDE completion,
 * refactorable generated APIs, and deterministic generated source. Compile-time
 * processing provides those properties.</p>
 *
 * <p>Generation also makes the output inspectable. A developer can open
 * {@code CustomerEditFormModel} or {@code CustomerRowTableDescriptor} and see
 * exactly what the annotation input produced. That is much easier to debug
 * than a runtime scanner that builds hidden metadata after the application
 * starts.</p>
 *
 * <p>Inspectable output is especially useful for newcomers. The generated
 * source shows which classes are meant to be imported by application code and
 * which runtime packages are involved. Reading one generated form and one
 * generated table usually explains the architecture faster than reading the
 * processor implementation.</p>
 *
 * <h2>Minimal Mental Model</h2>
 *
 * <p>The developer writes annotated source. The processor validates that source
 * and writes generated Java. Runtime code uses generated Java plus
 * {@code cz.auderis.corusco.core} and {@code cz.auderis.corusco.swing}. The
 * annotations themselves are not scanned at runtime.</p>
 *
 * <pre>{@code
 * @SwingForm(id = "customer/edit")
 * record CustomerEdit(
 *         @TextField @Required String name,
 *         @ComboBox CustomerType type
 * ) {
 * }
 *
 * // After compilation, application code can use generated classes:
 * CustomerEditFormModel model = new CustomerEditFormModel(customer);
 * CustomerEditBindings.install(view, model, scope);
 * }</pre>
 *
 * <p>A generated class is build output. Do not edit it by hand. If the
 * generated API is not right, change the annotation input or the processor
 * contract, then regenerate.</p>
 *
 * <h2>What Gets Generated</h2>
 *
 * <p>Form annotations generate field keys, resource keys, field descriptions,
 * validation descriptions, problem codes, option descriptors, dependency
 * descriptors, a form model, a view interface, behavior-plan code, and a
 * bindings facade. The generated form model belongs to the core model layer;
 * the generated bindings facade belongs to the Swing installation layer.</p>
 *
 * <p>Table annotations generate a table key, column keys, column descriptions,
 * executable column objects, resource keys, a table descriptor, and Swing table
 * helper code. The table descriptor is Swing-neutral; Swing adapters use it to
 * configure a concrete {@code JTable}.</p>
 *
 * <p>Command annotations generate action keys, resource keys, action
 * descriptions, grouping data, and factories that create command objects bound
 * to an owner instance. The owner is usually a presenter or controller.</p>
 *
 * <p>Validation annotations generate declarative constraint descriptions and
 * validator wiring for supported field constraints. Complex business rules
 * should remain handwritten validators or presenter logic.</p>
 *
 * <p>Component-state and dependency annotations generate explicit state model
 * members and dependency companions. They describe what a presenter or binding
 * layer can control; they do not make the processor a GUI builder or hide
 * application workflow logic behind annotations.</p>
 *
 * <h2>Validation And Diagnostics</h2>
 *
 * <p>The processor checks that annotations are used on supported source
 * elements. For example, form sources must be non-generic records or abstract
 * classes with annotated abstract accessors, table sources must be records,
 * command methods must be no-argument {@code void} methods, and validation
 * annotations must be compatible with the field kind they annotate.</p>
 *
 * <p>Diagnostics should point to the source location a developer can fix. A
 * malformed field should report the record component or abstract accessor. A
 * malformed command should report the method. A generated-source failure after
 * valid input should be treated as a processor defect.</p>
 *
 * <h2>Build Configuration</h2>
 *
 * <p>A build normally places {@code corusco-annotations} on the compile
 * classpath and {@code corusco-processor} on the annotation-processor path.
 * Runtime code then depends on {@code corusco-core} and, for Swing screens,
 * {@code corusco-swing}.</p>
 *
 * <pre>{@code
 * // Gradle shape, using the version notation appropriate for the build:
 * implementation("cz.auderis.corusco:corusco-annotations")
 * annotationProcessor("cz.auderis.corusco:corusco-processor")
 * implementation("cz.auderis.corusco:corusco-core")
 * implementation("cz.auderis.corusco:corusco-swing")
 * }</pre>
 *
 * <p>If annotated source compiles but generated classes are missing, first
 * check that the processor artifact is actually configured as an annotation
 * processor. If generated files exist but the IDE cannot see them, check
 * generated-source registration in the build or IDE project model.</p>
 *
 * <h2>Testing Generated Contracts</h2>
 *
 * <p>Processor tests should compile small sample source files and inspect the
 * generated output. This tests the same contract an application developer uses:
 * source annotations in, generated Java out.</p>
 *
 * <pre>{@code
 * GeneratedSourceCompilation compilation = GeneratedSourceCompiler.in(tempDir)
 *         .withProcessor(new CoruscoAnnotationProcessor())
 *         .compile("demo/CustomerEdit.java", sourceText);
 *
 * compilation.assertGeneratedSourceContains(
 *         "demo/CustomerEditFormModel.java",
 *         "extends AbstractFormModel"
 * );
 * }</pre>
 *
 * <p>Use {@code cz.auderis.corusco.test} for the public compiler harness. It
 * keeps processor tests focused on generated contracts instead of copying javac
 * setup code into every test.</p>
 *
 * <h2>Debugging Checklist</h2>
 *
 * <ul>
 *   <li>Confirm that the processor is on the annotation-processor path.</li>
 *   <li>Read javac diagnostics before inspecting generated files.</li>
 *   <li>Check the generated-source directory for expected companion names.</li>
 *   <li>Confirm that generated sources are included in the same compilation
 *       model that consumes them.</li>
 *   <li>Inspect generated Java when the runtime behavior is surprising.</li>
 * </ul>
 *
 * <h2>Boundaries</h2>
 *
 * <p>The processor is not a runtime framework, a dependency-injection
 * container, or a GUI builder. It generates stable Java support code from
 * annotated source. Layout, service calls, custom renderers, permissions, and
 * application workflows remain application code.</p>
 */
package cz.auderis.corusco.processor;
