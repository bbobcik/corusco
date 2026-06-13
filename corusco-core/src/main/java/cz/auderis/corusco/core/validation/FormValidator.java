package cz.auderis.corusco.core.validation;

import cz.auderis.corusco.core.problem.ProblemSet;

/**
 * Validates a whole form or model object.
 *
 * <p>Form validators are used for cross-field rules. Dependency metadata lives
 * on the enclosing {@link ValidationRule} so generated code can expose which
 * field changes should trigger a rule.</p>
 *
 * @param <M> model type
 */
@FunctionalInterface
public interface FormValidator<M> {

    /**
     * Validates the supplied model.
     *
     * @param model model to validate
     * @return validation problems
     */
    ProblemSet validate(M model);
}
