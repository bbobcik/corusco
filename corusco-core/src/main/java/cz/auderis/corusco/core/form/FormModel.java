package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.problem.ProblemSet;

/**
 * Non-Swing form model contract.
 *
 * @param <R> committed result type
 */
public interface FormModel<R> {

    /**
     * Returns aggregated form problems.
     *
     * @return problem set
     */
    ProblemSet problems();

    /**
     * Indicates whether the form can produce a result.
     *
     * @return {@code true} when no blocking error problems are present
     */
    boolean isCommittable();

    /**
     * Resets the form to its original baseline.
     */
    void reset();

    /**
     * Accepts current semantic values as the new dirty-state baseline.
     */
    void acceptCurrentValues();

    /**
     * Creates the committed result or throws when the form is not committable.
     *
     * @return committed result
     */
    R toResult();
}
