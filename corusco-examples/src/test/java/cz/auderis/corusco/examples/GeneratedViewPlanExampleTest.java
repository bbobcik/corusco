package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedViewPlanExampleTest {

    @Test
    void generatedBehaviorPlanWiresViewAndModel() {
        assertThat(GeneratedViewPlanExample.runScenario()).containsExactly("Bob", "false", "no-tooltip");
    }
}
