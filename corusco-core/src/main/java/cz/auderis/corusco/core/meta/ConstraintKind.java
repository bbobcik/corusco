package cz.auderis.corusco.core.meta;

/**
 * Category of validation constraint recorded in generated field metadata.
 *
 * <p>The processor emits these values from validation annotations so tools,
 * forms, and tests can describe the validation contract without re-reading
 * source annotations. The enum identifies the kind of constraint, while the
 * generated descriptor carries the constraint parameters such as bounds or
 * regular-expression text. Values are stable metadata identifiers and should
 * not be renamed when descriptor output is consumed externally.</p>
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
    DECIMAL_RANGE,

    /**
     * Integer numeric range.
     */
    INT_RANGE,

    /**
     * Regular-expression match.
     */
    REGEX
}
