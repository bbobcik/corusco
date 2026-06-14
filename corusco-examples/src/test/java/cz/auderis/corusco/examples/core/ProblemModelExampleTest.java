package cz.auderis.corusco.examples.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemModelExampleTest {

    @Test
    void exampleFiltersProblemsByTypedFieldKey() {
        assertThat(ProblemModelExample.nameErrorMessages())
                .containsExactly("Customer name is required");
    }

    @Test
    void exampleOrdersProblemsBySeverity() {
        assertThat(ProblemModelExample.messagesBySeverity())
                .containsExactly("Customer name is required", "Credit limit should be reviewed");
    }
}
