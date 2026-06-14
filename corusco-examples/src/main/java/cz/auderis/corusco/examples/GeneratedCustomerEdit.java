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

/**
 * Annotated sample edit record used by generated form examples.
 *
 * <p>The record is intentionally package-private because it is not a framework
 * entry point. It demonstrates how a domain-facing edit DTO can carry
 * {@link SwingForm} metadata, field annotations, validation annotations, and
 * help metadata that the processor turns into generated field descriptors,
 * resource keys, problem codes, and Swing component keys.</p>
 */
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
