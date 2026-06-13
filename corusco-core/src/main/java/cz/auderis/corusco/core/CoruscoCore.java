package cz.auderis.corusco.core;

/**
 * Baseline marker for the Corusco core module.
 */
public final class CoruscoCore {

    private static final String MODULE_NAME = "corusco-core";

    private CoruscoCore() {
        throw new AssertionError("No instances");
    }

    /**
     * Returns the stable Gradle module name for smoke tests and diagnostics.
     *
     * @return the core module name
     */
    public static String moduleName() {
        return MODULE_NAME;
    }
}
