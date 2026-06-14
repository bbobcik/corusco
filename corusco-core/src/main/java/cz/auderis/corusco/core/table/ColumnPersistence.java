package cz.auderis.corusco.core.table;

/**
 * Persistence metadata for one table column.
 *
 * <p>The persistence id is the stable token used by table-state storage. It
 * defaults to the generated column id, but generated code may override it when
 * a UI column must migrate independently from the row-value identity. Width
 * bounds are declarative clamp limits for later table-state restoration.</p>
 *
 * <p>Generated {@code @Column} metadata creates {@code ColumnPersistence}
 * instances inside row-specific columns companions such as
 * {@code CustomerRowColumns} from {@code persistenceId},
 * {@code minWidth}, and {@code maxWidth} annotation members.</p>
 *
 * @param id stable persistence id
 * @param minWidth minimum restored width in pixels
 * @param maxWidth maximum restored width in pixels
 */
public record ColumnPersistence(String id, int minWidth, int maxWidth) {

    /**
     * Creates persistence metadata.
     *
     * @param id stable persistence id
     * @param minWidth minimum restored width in pixels
     * @param maxWidth maximum restored width in pixels
     */
    public ColumnPersistence {
        id = TableIds.requireId(id);
        if (minWidth <= 0) {
            throw new IllegalArgumentException("minWidth must be greater than zero");
        }
        if (maxWidth < minWidth) {
            throw new IllegalArgumentException("maxWidth must be greater than or equal to minWidth");
        }
    }

    /**
     * Creates persistence metadata.
     *
     * @param id stable persistence id
     * @param minWidth minimum restored width in pixels
     * @param maxWidth maximum restored width in pixels
     * @return persistence metadata
     */
    public static ColumnPersistence of(String id, int minWidth, int maxWidth) {
        return new ColumnPersistence(id, minWidth, maxWidth);
    }
}
