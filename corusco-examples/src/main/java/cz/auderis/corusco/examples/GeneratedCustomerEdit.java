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
