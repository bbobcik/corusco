package cz.auderis.corusco.annotations.dataset;

/**
 * Missing-value representation for a data-set column.
 *
 * <p>The policy describes how generated storage and adapters should interpret
 * absence for a column. It is metadata, not a guarantee that a specific storage
 * implementation already uses that representation. Generated frames and
 * adapters should either honor the declared policy or fail clearly when they
 * cannot.</p>
 */
public enum MissingPolicy {

    /**
     * Values are always present or absence is outside the data-set contract.
     */
    NONE,

    /**
     * {@code null} reference values represent missing values.
     */
    NULL_VALUE,

    /**
     * {@code NaN} represents missing floating-point values.
     */
    NAN,

    /**
     * Missingness is tracked in a separate bitset or validity mask.
     */
    BITSET,

    /**
     * A domain-specific sentinel value represents missingness.
     */
    SENTINEL,

    /**
     * Missingness is represented by an application-defined mechanism.
     */
    CUSTOM
}
