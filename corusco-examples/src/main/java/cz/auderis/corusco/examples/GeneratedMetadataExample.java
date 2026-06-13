package cz.auderis.corusco.examples;

import cz.auderis.corusco.annotations.CheckBox;
import cz.auderis.corusco.annotations.ComboBox;
import cz.auderis.corusco.annotations.DecimalRange;
import cz.auderis.corusco.annotations.DateField;
import cz.auderis.corusco.annotations.Help;
import cz.auderis.corusco.annotations.IntRange;
import cz.auderis.corusco.annotations.Length;
import cz.auderis.corusco.annotations.Required;
import cz.auderis.corusco.annotations.Regex;
import cz.auderis.corusco.annotations.SwingForm;
import cz.auderis.corusco.annotations.TextField;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Demonstrates generated field metadata from annotations.
 */
public final class GeneratedMetadataExample {

    private GeneratedMetadataExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Reads generated metadata constants produced during example compilation.
     *
     * @return key and descriptor details
     */
    public static List<String> runScenario() {
        // The generated classes are ordinary source artifacts. Newcomers should
        // be able to inspect them in the build directory and see direct key,
        // resource, problem, and descriptor construction.
        return List.of(
                GeneratedCustomerEditFields.NAME.id(),
                GeneratedCustomerEditResources.NAME_LABEL.id(),
                GeneratedCustomerEditProblems.NAME_REQUIRED.id(),
                GeneratedCustomerEditDescriptors.NAME.helpTopic().id(),
                GeneratedCustomerEditDescriptors.CREDIT_LIMIT.constraints().getFirst().kind().name(),
                GeneratedCustomerEditDescriptors.AGE.constraints().getFirst().kind().name(),
                GeneratedCustomerEditDescriptors.VALID_FROM.kind().name(),
                GeneratedCustomerEditDescriptors.TYPE.kind().name(),
                GeneratedCustomerEditDescriptors.ACTIVE.kind().name()
        );
    }
}

@SwingForm(id = "generated-customer")
record GeneratedCustomerEdit(
        @TextField @Required @Length(max = 80) @Regex("[A-Za-z ]+") @Help(topic = "generated-customer/name") String name,
        @TextField @DecimalRange(min = "0.00") BigDecimal creditLimit,
        @TextField @IntRange(min = 0, max = 120) Integer age,
        @DateField LocalDate validFrom,
        @ComboBox GeneratedCustomerType type,
        @CheckBox boolean active
) {
}

enum GeneratedCustomerType {
    RETAIL,
    BUSINESS
}
