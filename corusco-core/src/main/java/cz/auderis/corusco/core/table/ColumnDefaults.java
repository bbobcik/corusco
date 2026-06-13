package cz.auderis.corusco.core.table;

/**
 * Default presentation state for a table column.
 *
 * <p>These defaults are declarative metadata for generated descriptors and
 * table-state initialization. They do not persist user customizations.</p>
 *
 * @param width preferred width in pixels
 * @param order default zero-based visual order
 * @param visible whether the column is initially visible
 */
public record ColumnDefaults(int width, int order, boolean visible) {

    /**
     * Creates column defaults.
     *
     * @param width preferred width in pixels, greater than zero
     * @param order default visual order, zero or greater
     * @param visible initial visibility
     */
    public ColumnDefaults {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than zero");
        }
        if (order < 0) {
            throw new IllegalArgumentException("order must not be negative");
        }
    }

    /**
     * Creates visible defaults.
     *
     * @param width preferred width in pixels
     * @param order default visual order
     * @return defaults
     */
    public static ColumnDefaults visible(int width, int order) {
        return new ColumnDefaults(width, order, true);
    }
}
