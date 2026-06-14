/**
 * Provides shared test support for Corusco processor and generated-source
 * checks.
 *
 * <p>The module wraps the Java compiler API so tests can compile generated
 * sources, inspect diagnostics, and assert processor behavior without copying
 * compiler setup code into each module. It is intended for test code and build
 * verification; production applications should not depend on it.</p>
 */
module cz.auderis.corusco.test {
    requires transitive java.compiler;

    exports cz.auderis.corusco.test;
}
