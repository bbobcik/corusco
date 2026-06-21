package cz.auderis.corusco.examples.core;

import cz.auderis.corusco.core.convert.Converters;
import cz.auderis.corusco.core.convert.EmptyTextPolicy;
import cz.auderis.corusco.core.form.AbstractFormModel;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.core.validation.RuleSet;
import cz.auderis.corusco.core.validation.Validators;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import java.math.BigDecimal;
import java.util.List;

/**
 * Demonstrates validation rules on handwritten form models.
 *
 * <p>The example applies required, length, range, and pattern validators to
 * core field models and reports typed problems. It is the handwritten
 * counterpart to generated validation metadata.</p>
 */
public final class ValidationExample {

    private static final TextFieldKey<CustomerEdit, String> NAME =
            TextFieldKey.of("customer/name", CustomerEdit.class, String.class);
    private static final TextFieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT =
            TextFieldKey.of("customer/credit-limit", CustomerEdit.class, BigDecimal.class);
    private static final ProblemCode CREDIT_LIMIT_REQUIRES_NAME =
            ProblemCode.of("validation/customer-name-for-credit-limit");

    private ValidationExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Returns validation messages for a sample form.
     *
     * @return validation messages
     */
    public static List<String> validationMessages() {
        CustomerForm form = new CustomerForm(new CustomerEdit("", new BigDecimal("10.00")));

        // This edit parses successfully, so field validation can run. If the
        // text were invalid, parse problems would be reported separately and
        // the credit-limit validator would be skipped.
        form.creditLimit.setRawText("2000.00", StandardChangeOrigin.USER);

        return form.problems().problems().stream()
                .map(Problem::message)
                .toList();
    }

    private record CustomerEdit(String name, BigDecimal creditLimit) {
    }

    private static final class CustomerForm extends AbstractFormModel<CustomerEdit> {

        private final TextFieldModel<CustomerEdit, String> name;
        private final TextFieldModel<CustomerEdit, BigDecimal> creditLimit;
        private final RuleSet<CustomerForm> rules;

        private CustomerForm(CustomerEdit original) {
            this.name = register(new TextFieldModel<>(NAME, original.name(), Converters.string()));
            this.creditLimit = register(new TextFieldModel<>(
                    CREDIT_LIMIT,
                    original.creditLimit(),
                    Converters.bigDecimal(EmptyTextPolicy.REJECT)
            ));
            this.rules = RuleSet.<CustomerForm>builder()
                    // Field validators target one key and can be re-run when
                    // that key changes.
                    .field(NAME.asFieldKey(), form -> form.name, Validators.required("Name is required"))
                    .field(CREDIT_LIMIT.asFieldKey(), form -> form.creditLimit,
                            Validators.decimalRange(BigDecimal.ZERO, new BigDecimal("1000.00"), "Credit limit is too high"))
                    // Cross-field validators declare all typed dependencies so
                    // generated bindings can revalidate them selectively later.
                    .form(List.of(NAME.asFieldKey(), CREDIT_LIMIT.asFieldKey()), form -> {
                        if (form.name.value().isBlank() && form.creditLimit.value().compareTo(BigDecimal.ZERO) > 0) {
                            return ProblemSet.of(Problem.validation(
                                    CREDIT_LIMIT_REQUIRES_NAME,
                                    ProblemSeverity.ERROR,
                                    ProblemTarget.form(),
                                    "Credit limit requires a customer name"
                            ));
                        }
                        return ProblemSet.empty();
                    })
                    .build();
        }

        @Override
        protected ProblemSet validationProblems() {
            return rules.validateAll(this);
        }

        @Override
        protected CustomerEdit createResult() {
            return new CustomerEdit(name.value(), creditLimit.value());
        }
    }
}
