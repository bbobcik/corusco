package cz.auderis.corusco.core.table;

/**
 * Persisted visual state for one table column.
 *
 * <p>The id matches {@link ColumnPersistence#id()}. Width is stored in pixels,
 * order is a zero-based visual order, and visible indicates whether the column
 * is currently visible. Runtime table-state controllers may derive instances
 * from Swing column models, while stores persist only these stable values.</p>
 *
 * @param id stable column persistence id
 * @param width column width in pixels
 * @param order zero-based visual order
 * @param visible whether the column is visible
 */
public record ColumnState(String id, int width, int order, boolean visible) {

    /**
     * Creates column state.
     *
     * @param id stable column persistence id
     * @param width column width in pixels
     * @param order zero-based visual order
     * @param visible whether the column is visible
     */
    public ColumnState {
        id = TableIds.requireId(id);
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than zero");
        }
        if (order < 0) {
            throw new IllegalArgumentException("order must not be negative");
        }
    }

    /**
     * Returns a copy with clamped width and normalized visual order.
     *
     * @param width new width
     * @param order new order
     * @return normalized state
     */
    ColumnState withWidthAndOrder(int width, int order) {
        return new ColumnState(id, width, order, visible);
    }
}
