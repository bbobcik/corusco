package cz.auderis.corusco.examples.book;

import cz.auderis.corusco.annotations.form.CoruscoForm;
import cz.auderis.corusco.annotations.form.TextField;
import cz.auderis.corusco.annotations.validation.Length;
import cz.auderis.corusco.annotations.validation.Required;

@CoruscoForm(id = "book/customer/edit")
public record CustomerEdit(
        @TextField
        @Required
        @Length(max = 80)
        String name,
        @TextField
        String status) {
}
