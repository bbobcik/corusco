package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObservableValueExampleTest {

    @Test
    void exampleShowsDerivedValueAndScopedCleanup() {
        assertThat(ObservableValueExample.runValueScenario())
                .containsExactly("Hello, Grace Lovelace");
    }
}
