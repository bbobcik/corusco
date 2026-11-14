package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.problem.ProblemSet;
import java.util.Objects;

/** Immutable aggregate state after a completed form mutation. */
public record FormState(ProblemSet problems, boolean committable, boolean dirty, long revision) {
    public FormState { Objects.requireNonNull(problems, "problems"); }
}
