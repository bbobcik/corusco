package cz.auderis.corusco.annotations;

/**
 * Baseline marker for the Corusco annotations module.
 */
public final class CoruscoAnnotations {

    private static final String MODULE_NAME = "corusco-annotations";

    private CoruscoAnnotations() {
        throw new AssertionError("No instances");
    }

    /**
     * Returns the stable Gradle module name for smoke tests and diagnostics.
     *
     * @return the annotations module name
     */
    public static String moduleName() {
        return MODULE_NAME;
    }
}
