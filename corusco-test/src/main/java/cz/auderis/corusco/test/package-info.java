/**
 * Shared test support for Corusco processor and generated-source tests.
 *
 * <p>This package is for tests, not production applications. Start with
 * {@link cz.auderis.corusco.test.GeneratedSourceCompiler} when a test needs to
 * compile sample Java source with Corusco annotation processors enabled and
 * inspect the generated files. {@link
 * cz.auderis.corusco.test.GeneratedSourceCompilation} exposes the compilation
 * result, diagnostics, generated-source root, and convenience assertions for
 * source snippets.</p>
 *
 * <p>{@link cz.auderis.corusco.test.CoruscoTestSupport} is a small module
 * marker and diagnostics utility. Swing-specific presenter/view testing lives
 * in {@code cz.auderis.corusco.swing.testing}; this package focuses on compiler
 * harnesses and generated-source verification.</p>
 *
 * <p>The compiler harness writes under a caller-owned temporary directory and
 * requires a JDK with the system compiler available. Tests own cleanup through
 * their temporary-directory mechanism.</p>
 */
package cz.auderis.corusco.test;
