package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwingMvpTesterProblemExampleTest {

    @Test
    void swingMvpTesterProblemExampleAssertsTypedProblems() {
        assertThat(SwingMvpTesterProblemExample.runScenario()).containsExactly("1", "0", "Alice");
    }
}
