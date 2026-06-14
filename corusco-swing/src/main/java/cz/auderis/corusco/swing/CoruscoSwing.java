package cz.auderis.corusco.swing;

/**
 * Identifies the Corusco Swing integration module in smoke tests and
 * diagnostics.
 *
 * <p>This class has no binding or component-factory behavior. Applications
 * should use the focused Swing packages for bindings, behaviors, dialogs,
 * tables, tasks, and test support. The marker only exposes the module name used
 * by smoke tests and simple runtime diagnostics.</p>
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
