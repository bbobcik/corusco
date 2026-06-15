package cz.auderis.corusco.examples.generation;

import cz.auderis.corusco.annotations.form.SwingForm;
import cz.auderis.corusco.annotations.form.TextField;
import cz.auderis.corusco.annotations.validation.Length;
import cz.auderis.corusco.annotations.validation.Required;

@SwingForm(id = "book/customer/edit")
public record CustomerEdit(
        @TextField
        @Required
        @Length(max = 80)
        String name,
        @TextField
        String status) {
}
