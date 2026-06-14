package cz.auderis.corusco.examples.swing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwingTaskExampleTest {

    @Test
    void swingTaskExampleDeliversCallbacksOnEdt() {
        assertThat(SwingTaskExample.runScenario()).containsExactly(
                "taskEdt=false",
                "callbackEdt=true",
                "value=loaded"
        );
    }
}
