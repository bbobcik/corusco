package cz.auderis.corusco.core.validation;

/**
 * Timing hint for validation rules.
 *
 * <p>The current core executes rules synchronously when asked. Timing metadata
 * is preserved so later Swing bindings and generated plans can decide whether a
 * rule belongs to change-time validation, commit-time validation, or a future
 * debounced path.</p>
 */
public enum ValidationTiming {

    /**
     * Rule is suitable for immediate validation after a relevant value changes.
     */
    ON_CHANGE,

    /**
     * Rule should be evaluated before committing a form result.
     */
    ON_COMMIT
}
