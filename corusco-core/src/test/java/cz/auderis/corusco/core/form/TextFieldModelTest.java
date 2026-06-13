package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.convert.Converters;
import cz.auderis.corusco.core.convert.EmptyTextPolicy;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.core.problem.ProblemFilter;
import cz.auderis.corusco.core.value.ChangeOrigin;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextFieldModelTest {

    private static final TextFieldKey<CustomerEdit, String> NAME =
            TextFieldKey.of("customer/name", CustomerEdit.class, String.class);
    private static final TextFieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT =
            TextFieldKey.of("customer/credit-limit", CustomerEdit.class, BigDecimal.class);
    private static final FieldKey<CustomerEdit, Boolean> ACTIVE =
            FieldKey.of("customer/active", CustomerEdit.class, Boolean.class);

    @Test
    void parseSuccessUpdatesSemanticValueAndDirtyState() {
        TextFieldModel<CustomerEdit, BigDecimal> field =
                new TextFieldModel<>(CREDIT_LIMIT, new BigDecimal("10.00"), Converters.bigDecimal(EmptyTextPolicy.REJECT));

        field.setRawText("20.00", ChangeOrigin.USER);

        assertThat(field.value()).isEqualByComparingTo("20.00");
        assertThat(field.rawText().value()).isEqualTo("20.00");
        assertThat(field.parseState().value()).isInstanceOf(ParseState.Parsed.class);
        assertThat(field.problems().isEmpty()).isTrue();
        assertThat(field.isDirty()).isTrue();
        assertThat(field.isTouched()).isTrue();
    }

    @Test
    void parseFailureKeepsPreviousSemanticValueAndStoresRawText() {
        TextFieldModel<CustomerEdit, BigDecimal> field =
                new TextFieldModel<>(CREDIT_LIMIT, new BigDecimal("10.00"), Converters.bigDecimal(EmptyTextPolicy.REJECT));

        field.setRawText("not-a-number", ChangeOrigin.USER);

        assertThat(field.value()).isEqualByComparingTo("10.00");
        assertThat(field.rawText().value()).isEqualTo("not-a-number");
        assertThat(field.parseState().value()).isInstanceOf(ParseState.Failed.class);
        assertThat(field.problems().filter(ProblemFilter.field(CREDIT_LIMIT.asFieldKey())).size()).isEqualTo(1);
        assertThat(field.isDirty()).isFalse();
        assertThat(field.isTouched()).isTrue();
    }

    @Test
    void emptyNullPolicyIsConfigurable() {
        TextFieldModel<CustomerEdit, BigDecimal> nullable =
                new TextFieldModel<>(CREDIT_LIMIT, new BigDecimal("10.00"), Converters.bigDecimal(EmptyTextPolicy.NULL_VALUE));
        TextFieldModel<CustomerEdit, BigDecimal> required =
                new TextFieldModel<>(CREDIT_LIMIT, new BigDecimal("10.00"), Converters.bigDecimal(EmptyTextPolicy.REJECT));

        nullable.setRawText("", ChangeOrigin.USER);
        required.setRawText("", ChangeOrigin.USER);

        assertThat(nullable.value()).isNull();
        assertThat(nullable.problems().isEmpty()).isTrue();
        assertThat(required.value()).isEqualByComparingTo("10.00");
        assertThat(required.problems().hasErrors()).isTrue();
    }

    @Test
    void resetRestoresOriginalRawSemanticDirtyTouchedAndProblems() {
        TextFieldModel<CustomerEdit, BigDecimal> field =
                new TextFieldModel<>(CREDIT_LIMIT, new BigDecimal("10.00"), Converters.bigDecimal(EmptyTextPolicy.REJECT));

        field.setRawText("bad", ChangeOrigin.USER);
        field.reset();

        assertThat(field.value()).isEqualByComparingTo("10.00");
        assertThat(field.rawText().value()).isEqualTo("10.00");
        assertThat(field.problems().isEmpty()).isTrue();
        assertThat(field.isDirty()).isFalse();
        assertThat(field.isTouched()).isFalse();
    }

    @Test
    void acceptingCurrentValueUpdatesDirtyBaseline() {
        TextFieldModel<CustomerEdit, String> field =
                new TextFieldModel<>(NAME, "Ada", Converters.string());

        field.setRawText("Grace", ChangeOrigin.USER);
        field.acceptCurrentValue();

        assertThat(field.isDirty()).isFalse();
        assertThat(field.value()).isEqualTo("Grace");
    }

    @Test
    void formAggregatesProblemsAndBlocksResultCreation() {
        CustomerEditForm form = new CustomerEditForm(new CustomerEdit("Ada", new BigDecimal("10.00")));

        form.creditLimit.setRawText("bad", ChangeOrigin.USER);

        assertThat(form.problems().hasErrors()).isTrue();
        assertThat(form.isCommittable()).isFalse();
        assertThatThrownBy(form::toResult)
                .isInstanceOf(UncommittableFormException.class)
                .hasMessage("Form is not committable");
    }

    @Test
    void formResultUsesSemanticValuesAfterSuccessfulEdits() {
        CustomerEditForm form = new CustomerEditForm(new CustomerEdit("Ada", new BigDecimal("10.00")));

        form.name.setRawText("Grace", ChangeOrigin.USER);
        form.creditLimit.setRawText("20.00", ChangeOrigin.USER);

        assertThat(form.toResult()).isEqualTo(new CustomerEdit("Grace", new BigDecimal("20.00")));
        form.acceptCurrentValues();
        assertThat(form.name.isDirty()).isFalse();
        assertThat(form.creditLimit.isDirty()).isFalse();
    }

    @Test
    void formCanRegisterPlainSemanticFields() {
        CustomerEditForm form = new CustomerEditForm(new CustomerEdit("Ada", new BigDecimal("10.00")));

        form.active.setValue(true, ChangeOrigin.USER);
        form.acceptCurrentValues();
        form.active.setValue(false, ChangeOrigin.USER);
        form.reset();

        assertThat(form.active.value().value()).isTrue();
        assertThat(form.active.isDirty()).isFalse();
        assertThat(form.active.isTouched()).isFalse();
    }

    private record CustomerEdit(String name, BigDecimal creditLimit) {
    }

    private static final class CustomerEditForm extends AbstractFormModel<CustomerEdit> {

        private final TextFieldModel<CustomerEdit, String> name;
        private final TextFieldModel<CustomerEdit, BigDecimal> creditLimit;
        private final FieldModel<CustomerEdit, Boolean> active;

        private CustomerEditForm(CustomerEdit original) {
            this.name = register(new TextFieldModel<>(NAME, original.name(), Converters.string()));
            this.creditLimit = register(new TextFieldModel<>(
                    CREDIT_LIMIT,
                    original.creditLimit(),
                    Converters.bigDecimal(EmptyTextPolicy.REJECT)
            ));
            this.active = register(new FieldModel<>(ACTIVE, false));
        }

        @Override
        protected CustomerEdit createResult() {
            return new CustomerEdit(name.value(), creditLimit.value());
        }
    }
}
