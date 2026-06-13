package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedTableColumnsExampleTest {

    @Test
    void generatedTableColumnsExampleReadsProcessorOutput() {
        assertThat(GeneratedTableColumnsExample.runScenario()).containsExactly(
                "generated-customer-table/name",
                "generated-customer-table/name/header",
                "generated-customer-table/name/help",
                "generated-customer-table/name",
                "generated-customer-table/customer-name",
                "320",
                "generated-customer-table/orders",
                "2",
                "Integer",
                "5",
                "true",
                "Acme Corp",
                "1:Globex",
                "3"
        );
    }
}
