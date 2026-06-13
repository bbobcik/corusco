package cz.auderis.corusco.core.meta;

/**
 * Kind of generated field constraint metadata.
 */
public enum ConstraintKind {

    /**
     * Non-null and, for text, non-blank value.
     */
    REQUIRED,

    /**
     * String length range.
     */
    LENGTH,

    /**
     * Decimal numeric range.
     */
    DECIMAL_RANGE
}
