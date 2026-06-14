package cz.auderis.corusco.examples.testing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwingMvpTesterTableStateExampleTest {

    @Test
    void swingMvpTesterTableStateExampleAssertsCapturedState() {
        assertThat(SwingMvpTesterTableStateExample.runScenario())
                .containsExactly("orders", "140", "orders");
    }
}
