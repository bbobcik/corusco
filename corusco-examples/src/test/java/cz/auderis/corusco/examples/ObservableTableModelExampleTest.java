package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObservableTableModelExampleTest {

    @Test
    void runsScenario() {
        assertThat(ObservableTableModelExample.runScenario())
                .containsExactly("customers.name:Acme", "Acme Corp", "3");
    }
}
