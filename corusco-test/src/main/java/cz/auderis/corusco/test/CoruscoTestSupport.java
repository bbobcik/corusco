package cz.auderis.corusco.test;

/**
 * Baseline marker for the Corusco test-support module.
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
