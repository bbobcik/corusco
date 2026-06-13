package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlazedListsInteropExampleTest {

    @Test
    void runsScenario() {
        assertThat(GlazedListsInteropExample.runScenario())
                .containsExactly("1", "1", "alpha,bravo,gamma", "alpha,bravo,gamma,delta");
    }
}
