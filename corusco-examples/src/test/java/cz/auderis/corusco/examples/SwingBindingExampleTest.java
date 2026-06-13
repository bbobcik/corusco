package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwingBindingExampleTest {

    @Test
    void exampleRunsSwingBindingsOnEdt() {
        assertThat(SwingBindingExample.runScenario())
                .containsExactly("25.00", "25.00", "true", "25.00");
    }
}
