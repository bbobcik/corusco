package cz.auderis.corusco.core.validation;

import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.ProblemSet;

/**
 * Validates one typed field value.
 *
 * <p>Implementations return immutable problem sets. They should not inspect raw
 * text; parse state belongs to text field models and parse problems. Field
 * validators run only after the caller has confirmed the field has no parse
 * errors.</p>
 *
 * @param <O> owner/model type
 * @param <T> field value type
 */
@FunctionalInterface
public interface FieldValidator<O, T> {

    /**
     * Validates a field value.
     *
     * @param key typed field key
     * @param value semantic value, possibly {@code null}
     * @return validation problems
     */
    ProblemSet validate(FieldKey<O, T> key, T value);
}
