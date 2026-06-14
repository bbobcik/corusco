package cz.auderis.corusco.examples.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationExampleTest {

    @Test
    void exampleShowsFieldAndCrossFieldValidationMessages() {
        assertThat(ValidationExample.validationMessages())
                .containsExactly(
                        "Name is required",
                        "Credit limit is too high",
                        "Credit limit requires a customer name"
                );
    }
}
