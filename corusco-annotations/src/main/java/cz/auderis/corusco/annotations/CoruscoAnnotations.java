package cz.auderis.corusco.annotations;

/**
 * Identifies the Corusco annotations module in smoke tests and diagnostics.
 *
 * <p>This class is not used by the annotation processor and does not describe
 * annotated source elements. It only exposes the published module name. Users
 * of the annotation API should start with {@link SwingForm},
 * {@link SwingTable}, field-kind annotations, constraint annotations, and
 * {@link UiAction}.</p>
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
