package cz.auderis.corusco.core.validation;

/**
 * Timing hint for validation rules.
 *
 * <p>The core executes rules synchronously when asked. Timing metadata lets
 * form models, Swing bindings, and generated plans separate rules that should
 * react to value changes from rules that should run during an explicit commit
 * attempt.</p>
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
