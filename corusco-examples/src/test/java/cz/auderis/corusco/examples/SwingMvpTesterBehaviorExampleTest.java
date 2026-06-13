package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwingMvpTesterBehaviorExampleTest {

    @Test
    void swingMvpTesterBehaviorExampleAssertsInstalledBehaviors() {
        assertThat(SwingMvpTesterBehaviorExample.runScenario()).containsExactly("2", "0");
    }
}
