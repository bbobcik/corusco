package cz.auderis.corusco.processor;

/**
 * Identifies the Corusco annotation processor module in smoke tests and
 * diagnostics.
 *
 * <p>This marker is separate from {@link CoruscoAnnotationProcessor}, which is
 * the compiler-discovered processor implementation. Applications normally use
 * this module through annotation-processor configuration rather than by calling
 * this marker directly.</p>
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
