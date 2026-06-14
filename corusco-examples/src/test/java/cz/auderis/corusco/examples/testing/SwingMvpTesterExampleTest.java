package cz.auderis.corusco.examples.testing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwingMvpTesterExampleTest {

    @Test
    void swingMvpTesterExampleFindsGeneratedComponents() {
        assertThat(SwingMvpTesterExample.runScenario()).containsExactly("Alice", "true", "VIP", "true", "1", "true");
    }
}
