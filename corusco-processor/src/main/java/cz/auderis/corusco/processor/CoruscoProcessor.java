package cz.auderis.corusco.processor;

/**
 * Baseline marker for the Corusco annotation processor module.
 */
public final class CoruscoProcessor {

    private static final String MODULE_NAME = "corusco-processor";

    private CoruscoProcessor() {
        throw new AssertionError("No instances");
    }

    /**
     * Returns the stable Gradle module name for smoke tests and diagnostics.
     *
     * @return the processor module name
     */
    public static String moduleName() {
        return MODULE_NAME;
    }
}
