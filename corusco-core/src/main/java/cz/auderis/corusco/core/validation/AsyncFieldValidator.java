package cz.auderis.corusco.core.validation;

import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.task.CancellationToken;

/**
 * Asynchronous validator for one typed field value.
 *
 * <p>Implementations perform validation that may block or take long enough to
 * run through a {@link cz.auderis.corusco.core.task.TaskService}. The caller
 * supplies the field key, current semantic value, and cancellation token for
 * the validation generation being evaluated. Validators should return a
 * {@link ProblemSet} describing validation findings and should not mutate the
 * owning form model directly.</p>
 *
 * <p>The interface does not prescribe a thread. Implementations that use
 * external services, caches, or Swing state must follow those collaborators'
 * threading rules and use the cancellation token to avoid publishing stale
 * results after a newer field value supersedes the current validation.</p>
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
