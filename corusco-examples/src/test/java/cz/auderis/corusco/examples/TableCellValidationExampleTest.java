package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableCellValidationExampleTest {

    @Test
    void runsScenario() {
        assertThat(TableCellValidationExample.runScenario())
                .containsExactly("Customer name is required", "no-problem");
    }
}
