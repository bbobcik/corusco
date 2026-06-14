package cz.auderis.corusco.glazedlists;

/**
 * Marker and diagnostics utility for the optional Glazed Lists interop module.
 *
 * <p>The module contains adapters between Glazed Lists {@code EventList}
 * instances and Corusco observable-list APIs. Applications normally start with
 * {@link GlazedListsAdapters}; this marker exists for smoke tests and tooling
 * that need to verify that the optional integration artifact is present without
 * constructing an adapter.</p>
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
