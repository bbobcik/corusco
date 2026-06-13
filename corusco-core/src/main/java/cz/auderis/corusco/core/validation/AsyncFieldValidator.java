package cz.auderis.corusco.core.validation;

import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.task.CancellationToken;

/**
 * Asynchronous validator for one typed field value.
 *
 * @param <O> owner/model type
 * @param <T> field value type
 */
@FunctionalInterface
public interface AsyncFieldValidator<O, T> {

    /**
     * Validates a field value.
     *
     * <p>The validator runs through a {@link cz.auderis.corusco.core.task.TaskService}
     * and should periodically inspect the supplied cancellation token if it
     * performs long-running or repeated work.</p>
     *
     * @param key typed field key
     * @param value semantic value, possibly {@code null}
     * @param cancellation cancellation token
     * @return validation problems
     * @throws Exception when validation fails unexpectedly
     */
    ProblemSet validate(FieldKey<O, T> key, T value, CancellationToken cancellation) throws Exception;
}
