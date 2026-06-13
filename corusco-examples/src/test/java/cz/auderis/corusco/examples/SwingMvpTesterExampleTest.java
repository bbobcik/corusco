package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwingMvpTesterExampleTest {

    @Test
    void swingMvpTesterExampleFindsGeneratedComponents() {
        assertThat(SwingMvpTesterExample.runScenario()).containsExactly("Alice", "true", "true");
    }
}
