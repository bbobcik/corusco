package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.annotations.form.CheckBox;
import cz.auderis.corusco.annotations.form.ComboBox;
import cz.auderis.corusco.annotations.form.DateField;
import cz.auderis.corusco.annotations.form.CoruscoForm;
import cz.auderis.corusco.annotations.form.TextField;
import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.validation.DecimalRange;
import cz.auderis.corusco.annotations.validation.IntRange;
import cz.auderis.corusco.annotations.validation.Length;
import cz.auderis.corusco.annotations.validation.Regex;
import cz.auderis.corusco.annotations.validation.Required;
import java.math.BigDecimal;
import java.time.LocalDate;

@CoruscoForm(id = "showcase/customer")
record ShowcaseCustomerEdit(
        @TextField
        @Required
        @Length(max = 80)
        @Regex("[A-Za-z ]+")
        @Help(topic = "showcase/customer/name")
        String name,
        @TextField
        @DecimalRange(min = "0.00", max = "500000.00")
        @Help(topic = "showcase/customer/credit-limit")
        BigDecimal creditLimit,
        @TextField
        @IntRange(min = 0, max = 120)
        Integer age,
        @DateField
        @Required
        LocalDate validFrom,
        @ComboBox
        @Required
        ShowcaseCustomerType type,
        @CheckBox
        boolean active
) {
}
