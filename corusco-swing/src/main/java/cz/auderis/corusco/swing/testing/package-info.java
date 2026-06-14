/**
 * EDT-safe test harness helpers for Swing MVP views, presenters, and generated
 * component keys.
 *
 * <p>This package is for tests of Swing-facing presenter/view code. Start with
 * {@link cz.auderis.corusco.swing.testing.SwingMvpTester}, which creates an
 * EDT boundary for arranging components, driving presenter actions, and making
 * assertions without showing native windows. {@link
 * cz.auderis.corusco.swing.testing.SwingComponentKeys} supplies typed component
 * keys in the same style as generated view metadata.</p>
 *
 * <p>The helpers are not production UI infrastructure. They exist to keep
 * tests honest about Swing threading, component lookup, problem assertions,
 * behavior installation, selection state, and table state. Use them when a test
 * needs to verify the relationship between a presenter, a generated or
 * handwritten view contract, and Swing components.</p>
 *
 * <p>Production bindings and behavior scopes live in the neighboring
 * {@code binding}, {@code behavior}, {@code dialog}, and {@code table}
 * packages. This testing package should mirror their public contracts rather
 * than become a separate application framework.</p>
 */
package cz.auderis.corusco.swing.testing;
