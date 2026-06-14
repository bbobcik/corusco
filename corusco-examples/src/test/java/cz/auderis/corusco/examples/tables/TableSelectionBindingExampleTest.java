package cz.auderis.corusco.examples.tables;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableSelectionBindingExampleTest {

    @Test
    void runsScenario() {
        assertThat(TableSelectionBindingExample.runScenario())
                .containsExactly("1:Alice", "1:Bravo");
    }
}
