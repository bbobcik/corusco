package cz.auderis.corusco.core.convert;

/**
 * Defines how text converters treat an empty input string.
 *
 * <p>The policy is used by converters whose target type cannot naturally
 * represent an empty string, such as numbers and dates. It lets generated and
 * handwritten form models distinguish optional fields from required fields
 * without making the converter depend on validation rules.</p>
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
