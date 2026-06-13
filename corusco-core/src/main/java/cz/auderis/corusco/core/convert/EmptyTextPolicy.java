package cz.auderis.corusco.core.convert;

/**
 * Policy for parsing empty text in converters.
 */
public enum EmptyTextPolicy {

    /**
     * Empty text parses to {@code null}.
     */
    NULL_VALUE,

    /**
     * Empty text is rejected as a parse failure.
     */
    REJECT
}
