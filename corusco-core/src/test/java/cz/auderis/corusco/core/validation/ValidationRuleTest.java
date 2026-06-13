package cz.auderis.corusco.core.validation;

import cz.auderis.corusco.core.convert.Converters;
import cz.auderis.corusco.core.convert.EmptyTextPolicy;
import cz.auderis.corusco.core.form.AbstractFormModel;
import cz.auderis.corusco.core.form.FieldModel;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemFilter;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemSource;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.core.value.ChangeOrigin;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationRuleTest {

    private static final TextFieldKey<CustomerEdit, String> NAME =
            TextFieldKey.of("customer/name", CustomerEdit.class, String.class);
    private static final TextFieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT =
            TextFieldKey.of("customer/credit-limit", CustomerEdit.class, BigDecimal.class);
    private static final TextFieldKey<CustomerEdit, Integer> DISCOUNT =
            TextFieldKey.of("customer/discount", CustomerEdit.class, Integer.class);
    private static final TextFieldKey<CustomerEdit, LocalDate> VALID_FROM =
            TextFieldKey.of("customer/valid-from", CustomerEdit.class, LocalDate.class);
    private static final TextFieldKey<CustomerEdit, LocalDate> VALID_TO =
            TextFieldKey.of("customer/valid-to", CustomerEdit.class, LocalDate.class);
    private static final FieldKey<CustomerEdit, Boolean> ACTIVE =
            FieldKey.of("customer/active", CustomerEdit.class, Boolean.class);
    private static final ProblemCode DATE_RANGE = ProblemCode.of("validation/date-range");

    @Test
    void validatorsCoverCommonConstraints() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-13T00:00:00Z"), ZoneOffset.UTC);

        assertThat(Validators.<CustomerEdit, String>required("Name required")
                .validate(NAME.asFieldKey(), " ").hasErrors()).isTrue();
        assertThat(Validators.<CustomerEdit>length(2, 4, "Length")
                .validate(NAME.asFieldKey(), "ABCDE").hasErrors()).isTrue();
        assertThat(Validators.<CustomerEdit>decimalRange(BigDecimal.ZERO, new BigDecimal("10"), "Range")
                .validate(CREDIT_LIMIT.asFieldKey(), new BigDecimal("11")).hasErrors()).isTrue();
        assertThat(Validators.<CustomerEdit>integerRange(1, 5, "Range")
                .validate(DISCOUNT.asFieldKey(), 6).hasErrors()).isTrue();
        assertThat(Validators.<CustomerEdit>regex(Pattern.compile("[A-Z]+"), "Pattern")
                .validate(NAME.asFieldKey(), "abc").hasErrors()).isTrue();
        assertThat(Validators.<CustomerEdit>past(clock, "Past")
                .validate(VALID_FROM.asFieldKey(), LocalDate.of(2026, 6, 13)).hasErrors()).isTrue();
        assertThat(Validators.<CustomerEdit>future(clock, "Future")
                .validate(VALID_TO.asFieldKey(), LocalDate.of(2026, 6, 13)).hasErrors()).isTrue();
        assertThat(Validators.<CustomerEdit>present(clock, "Present")
                .validate(VALID_TO.asFieldKey(), LocalDate.of(2026, 6, 14)).hasErrors()).isTrue();
    }

    @Test
    void fieldValidationRunsAfterParseSuccess() {
        CustomerForm form = new CustomerForm(new CustomerEdit("", new BigDecimal("10"), 1,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));

        ProblemSet problems = form.rules.validateAll(form);

        assertThat(problems.filter(ProblemFilter.field(NAME.asFieldKey())).problems())
                .singleElement()
                .extracting(Problem::source)
                .isEqualTo(ProblemSource.VALIDATION);
    }

    @Test
    void fieldValidationIsSkippedWhenParseFails() {
        CustomerForm form = new CustomerForm(new CustomerEdit("Ada", new BigDecimal("10"), 1,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));

        form.creditLimit.setRawText("not-decimal", ChangeOrigin.USER);

        assertThat(form.rules.validateAll(form).filter(ProblemFilter.field(CREDIT_LIMIT.asFieldKey())).isEmpty())
                .isTrue();
        assertThat(form.problems().filter(ProblemFilter.source(ProblemSource.PARSE)).size()).isEqualTo(1);
    }

    @Test
    void crossFieldRuleDeclaresDependenciesAndCanBeTargeted() {
        CustomerForm form = new CustomerForm(new CustomerEdit("Ada", new BigDecimal("10"), 1,
                LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)));

        List<FieldKey<?, ?>> dependencies = form.rules.rules().getLast().dependencies();

        assertThat(dependencies).containsExactly(VALID_FROM.asFieldKey(), VALID_TO.asFieldKey());
        assertThat(form.rules.validateFor(form, VALID_FROM.asFieldKey()).hasErrors()).isTrue();
        assertThat(form.rules.validateFor(form, NAME.asFieldKey()).filter(ProblemFilter.form()).isEmpty()).isTrue();
    }

    @Test
    void formAggregatesParseAndValidationProblemsSeparately() {
        CustomerForm form = new CustomerForm(new CustomerEdit("", new BigDecimal("10"), 1,
                LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)));
        form.creditLimit.setRawText("bad", ChangeOrigin.USER);

        ProblemSet problems = form.problems();

        assertThat(problems.filter(ProblemFilter.source(ProblemSource.PARSE)).size()).isEqualTo(1);
        assertThat(problems.filter(ProblemFilter.source(ProblemSource.VALIDATION)).size()).isEqualTo(2);
        assertThat(form.isCommittable()).isFalse();
    }

    @Test
    void timingMetadataCanFilterRules() {
        CustomerForm form = new CustomerForm(new CustomerEdit("Ada", new BigDecimal("10"), 1,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));

        assertThat(form.rules.validateTiming(form, ValidationTiming.ON_COMMIT).isEmpty()).isTrue();
        assertThat(form.rules.validateTiming(form, ValidationTiming.ON_CHANGE).isEmpty()).isTrue();
    }

    @Test
    void semanticFieldRulesSupportNonTextFields() {
        CustomerForm form = new CustomerForm(new CustomerEdit("Ada", new BigDecimal("10"), 1,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));

        form.active.setValue(false, ChangeOrigin.USER);

        assertThat(form.rules.validateFor(form, ACTIVE).filter(ProblemFilter.field(ACTIVE)).problems())
                .singleElement()
                .extracting(Problem::message)
                .isEqualTo("Customer must be active");
    }

    private record CustomerEdit(
            String name,
            BigDecimal creditLimit,
            Integer discount,
            LocalDate validFrom,
            LocalDate validTo
    ) {
    }

    private static final class CustomerForm extends AbstractFormModel<CustomerEdit> {

        private final TextFieldModel<CustomerEdit, String> name;
        private final TextFieldModel<CustomerEdit, BigDecimal> creditLimit;
        private final TextFieldModel<CustomerEdit, Integer> discount;
        private final TextFieldModel<CustomerEdit, LocalDate> validFrom;
        private final TextFieldModel<CustomerEdit, LocalDate> validTo;
        private final FieldModel<CustomerEdit, Boolean> active;
        private final RuleSet<CustomerForm> rules;

        private CustomerForm(CustomerEdit original) {
            this.name = register(new TextFieldModel<>(NAME, original.name(), Converters.string()));
            this.creditLimit = register(new TextFieldModel<>(
                    CREDIT_LIMIT,
                    original.creditLimit(),
                    Converters.bigDecimal(EmptyTextPolicy.REJECT)
            ));
            this.discount = register(new TextFieldModel<>(DISCOUNT, original.discount(), Converters.integer()));
            this.validFrom = register(new TextFieldModel<>(
                    VALID_FROM,
                    original.validFrom(),
                    Converters.localDate(EmptyTextPolicy.REJECT)
            ));
            this.validTo = register(new TextFieldModel<>(
                    VALID_TO,
                    original.validTo(),
                    Converters.localDate(EmptyTextPolicy.REJECT)
            ));
            this.active = register(new FieldModel<>(ACTIVE, true));
            this.rules = RuleSet.<CustomerForm>builder()
                    .field(NAME.asFieldKey(), form -> form.name, Validators.required("Name required"))
                    .field(CREDIT_LIMIT.asFieldKey(), form -> form.creditLimit,
                            Validators.decimalRange(BigDecimal.ZERO, new BigDecimal("1000"), "Credit limit out of range"))
                    .field(DISCOUNT.asFieldKey(), form -> form.discount,
                            Validators.integerRange(0, 100, "Discount out of range"))
                    .semanticField(ACTIVE, form -> form.active, (key, value) -> Boolean.TRUE.equals(value)
                            ? ProblemSet.empty()
                            : ProblemSet.of(Problem.validation(
                                    ProblemCode.of("validation/active"),
                                    ProblemSeverity.ERROR,
                                    ProblemTarget.field(key),
                                    "Customer must be active"
                            )))
                    .form(List.of(VALID_FROM.asFieldKey(), VALID_TO.asFieldKey()), form -> {
                        if (form.validFrom.value().isAfter(form.validTo.value())) {
                            return ProblemSet.of(Problem.validation(
                                    DATE_RANGE,
                                    ProblemSeverity.ERROR,
                                    ProblemTarget.form(),
                                    "Valid-from must not be after valid-to"
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
            return new CustomerEdit(
                    name.value(),
                    creditLimit.value(),
                    discount.value(),
                    validFrom.value(),
                    validTo.value()
            );
        }
    }
}
