package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.convert.Converters;
import cz.auderis.corusco.core.convert.EmptyTextPolicy;
import cz.auderis.corusco.core.form.AbstractFormModel;
import cz.auderis.corusco.core.form.TextFieldModel;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.core.value.ChangeOrigin;
import java.math.BigDecimal;
import java.util.List;

/**
 * Demonstrates handwritten field and form models without Swing.
 */
public final class FieldModelExample {

    private static final TextFieldKey<CustomerEdit, String> NAME =
            TextFieldKey.of("customer/name", CustomerEdit.class, String.class);
    private static final TextFieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT =
            TextFieldKey.of("customer/credit-limit", CustomerEdit.class, BigDecimal.class);

    private FieldModelExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Demonstrates that invalid text does not destroy the previous semantic
     * value.
     *
     * @return diagnostics describing raw text, semantic value, and problems
     */
    public static List<String> singleFieldDiagnostics() {
        TextFieldModel<CustomerEdit, BigDecimal> creditLimit =
                new TextFieldModel<>(CREDIT_LIMIT, new BigDecimal("10.00"), Converters.bigDecimal(EmptyTextPolicy.REJECT));

        // The user can type an invalid intermediate value. The raw text changes
        // immediately, but the semantic value remains the last parse success.
        creditLimit.setRawText("12,", ChangeOrigin.USER);

        return List.of(
                creditLimit.rawText().value(),
                creditLimit.value().toPlainString(),
                String.valueOf(creditLimit.problems().hasErrors())
        );
    }

    /**
     * Demonstrates a small record-backed handwritten form.
     *
     * @return committed edit record after successful text edits
     */
    public static CustomerEdit editCustomer() {
        CustomerEditForm form = new CustomerEditForm(new CustomerEdit("Ada", new BigDecimal("10.00")));

        // The form is still plain Java: generated code should eventually emit
        // this shape, but no processor is needed to prove the core behavior.
        form.name.setRawText("Grace", ChangeOrigin.USER);
        form.creditLimit.setRawText("20.00", ChangeOrigin.USER);

        // toResult() is guarded by form committability; parse errors would
        // throw instead of producing a partially invalid record.
        return form.toResult();
    }

    /**
     * Sample edit record used by the example.
     *
     * @param name customer name
     * @param creditLimit customer credit limit
     */
    public record CustomerEdit(String name, BigDecimal creditLimit) {
    }

    private static final class CustomerEditForm extends AbstractFormModel<CustomerEdit> {

        private final TextFieldModel<CustomerEdit, String> name;
        private final TextFieldModel<CustomerEdit, BigDecimal> creditLimit;

        private CustomerEditForm(CustomerEdit original) {
            this.name = register(new TextFieldModel<>(NAME, original.name(), Converters.string()));
            this.creditLimit = register(new TextFieldModel<>(
                    CREDIT_LIMIT,
                    original.creditLimit(),
                    Converters.bigDecimal(EmptyTextPolicy.REJECT)
            ));
        }

        @Override
        protected CustomerEdit createResult() {
            return new CustomerEdit(name.value(), creditLimit.value());
        }
    }
}
