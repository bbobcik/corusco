package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.problem.ProblemSet;

/**
 * UI-independent contract for a form that can validate, reset, and produce a
 * committed result.
 *
 * <p>A form model is the semantic counterpart to a Swing dialog or generated
 * editor: it owns field values and problem state, while view bindings decide how
 * those values are displayed. Callers use {@link #problems()} and
 * {@link #isCommittable()} to decide whether submission is allowed, use
 * {@link #reset()} to return to the current baseline, and call
 * {@link #toResult()} only when a result should be materialized.</p>
 *
 * <p>Implementors define what the baseline means, how dirty state is tracked,
 * and when validation is refreshed. They must keep {@link #isCommittable()} and
 * {@link #toResult()} consistent: if the model is not committable, requesting a
 * result should fail instead of silently returning a partial object. The
 * interface does not impose threading; Swing callers normally invoke it from
 * the event dispatch thread because their bindings and components are
 * EDT-confined.</p>
 *
 * @param <R> committed result type
 */
public interface FormModel<R> {

    /**
     * Returns aggregated form problems for the current model state.
     *
     * @return problem set, never {@code null}
     */
    ProblemSet problems();

    /**
     * Indicates whether the form can produce a result.
     *
     * @return {@code true} when no blocking error problems are present
     */
    boolean isCommittable();

    /**
     * Resets the form to its current baseline.
     *
     * <p>Implementations may mutate field values and problem state. If the
     * model exposes observable values, those observables are expected to report
     * the resulting changes according to their own contracts.</p>
     */
    void reset();

    /**
     * Accepts current semantic values as the new dirty-state baseline.
     *
     * <p>This operation should not change the committed value represented by
     * the form; it only changes what later calls to {@link #reset()} consider
     * original state.</p>
     */
    void acceptCurrentValues();

    /**
     * Creates the committed result or throws when the form is not committable.
     *
     * @return committed result
     * @throws UncommittableFormException if blocking problems prevent commit
     */
    R toResult();
}
