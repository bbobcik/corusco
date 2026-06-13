package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BehaviorExampleTest {

    @Test
    void exampleInstallsBehaviorBasedBindingAndDecoration() {
        assertThat(BehaviorExample.runScenario())
                .containsExactly(
                        "bad",
                        "10",
                        """
                                Expected BigDecimal
                                Save is disabled until the field is valid
                                Customer display name
                                Press F1 for help""",
                        "generated-customer-table/name"
                );
    }
}
