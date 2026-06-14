package cz.auderis.corusco.examples.swing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusTextExampleTest {

    @Test
    void statusTextExamplePublishesFocusScopedGuidance() {
        assertThat(StatusTextExample.runScenario()).containsExactly(
                "Ready",
                "Enter the customer display name",
                "Ready"
        );
    }
}
