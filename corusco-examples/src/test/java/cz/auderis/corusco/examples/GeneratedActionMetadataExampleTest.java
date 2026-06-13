package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedActionMetadataExampleTest {

    @Test
    void generatedActionMetadataExampleReadsProcessorOutput() {
        assertThat(GeneratedActionMetadataExample.runScenario()).containsExactly(
                "generated-customer/save",
                "generated-customer/save/text",
                "generated-customer/save/tooltip",
                "83",
                "true"
        );
    }
}
