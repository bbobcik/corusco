package cz.auderis.corusco.glazedlists;

/**
 * Marker for the optional Corusco Glazed Lists interop module.
 */
public final class CoruscoGlazedLists {

    private static final String MODULE_NAME = "corusco-glazedlists";

    private CoruscoGlazedLists() {
        throw new AssertionError("No instances");
    }

    /**
     * Returns the stable Gradle module name for smoke tests and diagnostics.
     *
     * @return the Glazed Lists interop module name
     */
    public static String moduleName() {
        return MODULE_NAME;
    }
}
