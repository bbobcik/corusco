package cz.auderis.corusco.swing;

/**
 * Baseline marker for the Corusco Swing module.
 */
public final class CoruscoSwing {

    private static final String MODULE_NAME = "corusco-swing";

    private CoruscoSwing() {
        throw new AssertionError("No instances");
    }

    /**
     * Returns the stable Gradle module name for smoke tests and diagnostics.
     *
     * @return the Swing module name
     */
    public static String moduleName() {
        return MODULE_NAME;
    }
}
