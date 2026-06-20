package cz.auderis.corusco.examples.generated;

import cz.auderis.corusco.annotations.form.CheckBox;
import cz.auderis.corusco.annotations.form.ComboBox;
import cz.auderis.corusco.annotations.validation.DecimalRange;
import cz.auderis.corusco.annotations.form.DateField;
import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.validation.IntRange;
import cz.auderis.corusco.annotations.validation.Length;
import cz.auderis.corusco.annotations.validation.Required;
import cz.auderis.corusco.annotations.validation.Regex;
import cz.auderis.corusco.annotations.form.CoruscoForm;
import cz.auderis.corusco.annotations.form.TextField;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Annotated sample edit record used by generated form examples.
 *
 * <p>The record is intentionally package-private because it is not a framework
 * entry point. It demonstrates how a domain-facing edit DTO can carry
 * {@link CoruscoForm} metadata, field annotations, validation annotations, and
 * help metadata that the processor turns into generated field descriptors,
 * resource keys, problem codes, and Swing component keys.</p>
 */
@CoruscoForm(id = "generated-customer")
public record GeneratedCustomerEdit(
        @TextField @Required @Length(max = 80) @Regex("[A-Za-z ]+") @Help(topic = "generated-customer/name") String name,
        @TextField @DecimalRange(min = "0.00") BigDecimal creditLimit,
        @TextField @IntRange(min = 0, max = 120) Integer age,
        @DateField LocalDate validFrom,
        @ComboBox GeneratedCustomerType type,
        @CheckBox boolean active
) {
}
