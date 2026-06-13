package cz.auderis.corusco.core.validation;

import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.ProblemSet;
import java.util.List;
import java.util.Objects;

/**
 * One immutable validation rule with typed dependency metadata.
 *
 * <p>Dependencies are field keys, not field-name strings. They allow callers to
 * re-run only rules affected by a changed field where that optimization is
 * useful.</p>
 *
 * @param dependencies typed field dependencies
 * @param timing validation timing hint
 * @param validator validation function
 * @param <M> model type
 */
public record ValidationRule<M>(
        List<FieldKey<?, ?>> dependencies,
        ValidationTiming timing,
        FormValidator<M> validator
) {

    /**
     * Creates a validation rule.
     *
     * @param dependencies typed field dependencies
     * @param timing validation timing hint
     * @param validator validation function
     */
    public ValidationRule {
        Objects.requireNonNull(dependencies, "dependencies");
        dependencies = List.copyOf(dependencies);
        Objects.requireNonNull(timing, "timing");
        Objects.requireNonNull(validator, "validator");
    }

    /**
     * Runs the validation rule.
     *
     * @param model model to validate
     * @return validation problems
     */
    public ProblemSet validate(M model) {
        return validator.validate(model);
    }

    /**
     * Indicates whether this rule depends on the supplied field key.
     *
     * @param key changed field key
     * @return {@code true} when this rule should be considered affected
     */
    public boolean dependsOn(FieldKey<?, ?> key) {
        Objects.requireNonNull(key, "key");
        return dependencies.contains(key);
    }
}
