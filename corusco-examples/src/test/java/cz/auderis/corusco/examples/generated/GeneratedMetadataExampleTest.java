package cz.auderis.corusco.examples.generated;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedMetadataExampleTest {

    @Test
    void generatedMetadataExampleReadsProcessorOutput() {
        assertThat(GeneratedMetadataExample.runScenario()).containsExactly(
                "generated-customer/name",
                "generated-customer/name/label",
                "generated-customer/name/required",
                "generated-customer/name",
                "DECIMAL_RANGE",
                "INT_RANGE",
                "DATE",
                "COMBO_BOX",
                "CHECK_BOX"
        );
    }
}
