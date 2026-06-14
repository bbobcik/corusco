package cz.auderis.corusco.test;

/**
 * Marker and diagnostics utility for the Corusco test-support module.
 *
 * <p>The test-support artifact provides small harnesses for compiling generated
 * source examples and asserting annotation-processor output. Production code
 * should not depend on this class; tests can use it as a stable module-presence
 * marker when verifying classpaths.</p>
 */
public final class CoruscoTestSupport {

    private static final String MODULE_NAME = "corusco-test";

    private CoruscoTestSupport() {
        throw new AssertionError("No instances");
    }

    /**
     * Returns the stable Gradle module name for smoke tests and diagnostics.
     *
     * @return the test-support module name
     */
    public static String moduleName() {
        return MODULE_NAME;
    }
}
