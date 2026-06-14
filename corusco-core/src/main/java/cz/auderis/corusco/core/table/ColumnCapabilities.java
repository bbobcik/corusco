package cz.auderis.corusco.core.table;

/**
 * Declared capabilities for one table column.
 *
 * <p>Capabilities describe what higher-level table infrastructure may allow.
 * They do not install sorting, filtering, editing, or visibility UI by
 * themselves.</p>
 *
 * <p>Generated {@code @Column} metadata creates {@code ColumnCapabilities}
 * instances inside row-specific columns companions such as
 * {@code CustomerRowColumns} from {@code sortable},
 * {@code filterable}, {@code editable}, and {@code hideable} annotation
 * members.</p>
 *
 * @param sortable whether the column can participate in sorting
 * @param filterable whether the column can participate in filtering
 * @param editable whether the column can be edited through a row updater
 * @param hideable whether users may hide the column in later table-state UI
 */
public record ColumnCapabilities(boolean sortable, boolean filterable, boolean editable, boolean hideable) {

    /**
     * Read-only, sortable/filterable, hideable column capabilities.
     *
     * @return common read-only capabilities
     */
    public static ColumnCapabilities readOnly() {
        return new ColumnCapabilities(true, true, false, true);
    }

    /**
     * Editable, sortable/filterable, hideable column capabilities.
     *
     * @return common editable capabilities
     */
    public static ColumnCapabilities editableColumn() {
        return new ColumnCapabilities(true, true, true, true);
    }
}
