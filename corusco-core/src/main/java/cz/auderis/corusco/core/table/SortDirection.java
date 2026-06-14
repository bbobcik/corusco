package cz.auderis.corusco.core.table;

/**
 * Direction of a table sort key.
 *
 * <p>{@link SortState} uses this enum to describe user-visible table ordering
 * and table-state persistence stores may keep the enum name as part of saved
 * state. New code should use {@link #ASCENDING} as the default direction when a
 * column becomes sorted unless a table descriptor or user action says
 * otherwise.</p>
 */
public enum SortDirection {

    /**
     * Ascending sort order.
     */
    ASCENDING,

    /**
     * Descending sort order.
     */
    DESCENDING
}
