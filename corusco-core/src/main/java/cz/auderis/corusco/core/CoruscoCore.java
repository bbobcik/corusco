package cz.auderis.corusco.core;

/**
 * Identifies the Corusco core runtime module in smoke tests and diagnostics.
 *
 * <p>This class is not a service locator or a framework entry point. It exists
 * so tests, examples, and diagnostic output can refer to the published Gradle
 * module name without duplicating the string. Applications should normally
 * start with the typed packages under {@code cz.auderis.corusco.core}, such as
 * forms, values, commands, validation, tables, and lifecycle primitives.</p>
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
