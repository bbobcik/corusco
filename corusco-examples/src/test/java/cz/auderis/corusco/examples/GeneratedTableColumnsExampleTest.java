package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedTableColumnsExampleTest {

    @Test
    void generatedTableColumnsExampleReadsProcessorOutput() {
        assertThat(GeneratedTableColumnsExample.runScenario()).containsExactly(
                "generated-customer-table/name",
                "generated-customer-table/name/header",
                "Integer",
                "5",
                "false",
                "3"
        );
    }
}
