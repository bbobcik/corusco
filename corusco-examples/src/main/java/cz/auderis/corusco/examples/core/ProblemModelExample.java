package cz.auderis.corusco.examples.core;

import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemFilter;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import java.math.BigDecimal;
import java.util.List;

/**
 * Demonstrates typed field targeting and problem filtering.
 *
 * <p>The example builds problem sets with field targets and filters them for
 * presentation. It shows why Corusco uses typed keys instead of string property
 * paths when routing validation feedback.</p>
 */
public final class ProblemModelExample {

    private static final FieldKey<CustomerEdit, String> NAME =
            FieldKey.of("customer/name", CustomerEdit.class, String.class);
    private static final FieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT =
            FieldKey.of("customer/credit-limit", CustomerEdit.class, BigDecimal.class);
    private static final ProblemCode REQUIRED = ProblemCode.of("required");
    private static final ProblemCode RANGE = ProblemCode.of("range");

    private ProblemModelExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Returns field-specific error messages for a sample form.
     *
     * @return diagnostic messages for name-field errors
     */
    public static List<String> nameErrorMessages() {
        ProblemSet problems = sampleProblems();

        // Field filters use FieldKey identity, not a string field name. This is
        // the key contract later generated validators should follow.
        ProblemFilter nameErrors = ProblemFilter.field(NAME)
                .and(ProblemFilter.severityAtLeast(ProblemSeverity.ERROR));

        return problems.filter(nameErrors).problems().stream()
                .map(Problem::message)
                .toList();
    }

    /**
     * Returns all problems ordered from most severe to least severe.
     *
     * @return severity-ordered diagnostic messages
     */
    public static List<String> messagesBySeverity() {
        // ProblemSet keeps insertion order, and explicit severity sorting gives
        // summaries a deterministic display order when they need one.
        return sampleProblems().bySeverityDescending().stream()
                .map(Problem::message)
                .toList();
    }

    private static ProblemSet sampleProblems() {
        Problem missingName = Problem.validation(
                REQUIRED,
                ProblemSeverity.ERROR,
                ProblemTarget.field(NAME),
                "Customer name is required"
        );
        Problem largeCreditLimit = Problem.validation(
                RANGE,
                ProblemSeverity.WARNING,
                ProblemTarget.field(CREDIT_LIMIT),
                "Credit limit should be reviewed"
        );

        // Adding returns a new immutable set, which makes ownership explicit
        // when forms aggregate field and form-level problems.
        return ProblemSet.empty()
                .add(missingName)
                .add(largeCreditLimit);
    }

    private record CustomerEdit(String name, BigDecimal creditLimit) {
    }
}
