package cz.auderis.corusco.core.meta;

/**
 * Semantic value shape edited by a generated editor family.
 */
public enum SelectionShape {

    /**
     * The editor modifies one scalar value.
     */
    SINGLE_VALUE,

    /**
     * The editor modifies a nullable scalar value.
     */
    NULLABLE_SINGLE_VALUE,

    /**
     * The editor modifies a boolean two-state value.
     */
    BOOLEAN_TWO_STATE,

    /**
     * The editor maps three UI states to semantic values.
     */
    THREE_STATE,

    /**
     * The editor modifies a set-like multi-selection value.
     */
    MULTI_SELECTION,

    /**
     * The editor modifies a bounded numeric value.
     */
    BOUNDED_NUMERIC_VALUE
}
