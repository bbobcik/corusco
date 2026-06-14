package cz.auderis.corusco.examples.forms;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedFormModelExampleTest {

    @Test
    void generatedFormModelExampleEditsAndCommitsRecord() {
        assertThat(GeneratedFormModelExample.runScenario()).containsExactly(
                "true",
                "Bob",
                "25.50",
                "45",
                "false",
                "Alice"
        );
    }
}
