package cz.auderis.corusco.annotations.dataset;

/**
 * Quality-state policy for a data-set column.
 *
 * <p>Quality policy is separate from missing-value policy. Missing values
 * describe absence; quality describes whether a present value is valid,
 * estimated, stale, suspect, or otherwise qualified. Generated descriptors
 * expose the policy consistently to renderers, exporters, and adapters.</p>
 */
public enum QualityPolicy {

    /**
     * No quality state is declared for the column.
     */
    NONE,

    /**
     * Quality is represented by bit flags.
     */
    FLAGS,

    /**
     * Quality is represented by a companion value or code.
     */
    VALUE,

    /**
     * Quality interpretation is application-defined.
     */
    CUSTOM
}
