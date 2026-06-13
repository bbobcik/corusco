package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwingMvpTesterTableExampleTest {

    @Test
    void swingMvpTesterTableExampleSelectsRowsByGeneratedKey() {
        assertThat(SwingMvpTesterTableExample.runScenario()).containsExactly("Alice", "1", "1");
    }
}
