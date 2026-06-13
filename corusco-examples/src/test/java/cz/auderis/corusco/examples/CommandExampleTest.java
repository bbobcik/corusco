package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandExampleTest {

    @Test
    void commandExampleRunsScenario() {
        assertThat(CommandExample.runScenario()).containsExactly("2", "false", "false");
    }
}
