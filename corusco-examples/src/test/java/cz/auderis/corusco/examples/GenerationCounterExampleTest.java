package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerationCounterExampleTest {

    @Test
    void generationCounterExampleIgnoresStaleResults() {
        assertThat(GenerationCounterExample.runScenario()).containsExactly(
                "ok",
                "beforeClearStale=true"
        );
    }
}
