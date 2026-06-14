package cz.auderis.corusco.examples.generated;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedViewPlanExampleTest {

    @Test
    void generatedBehaviorPlanWiresViewAndModel() {
        assertThat(GeneratedViewPlanExample.runScenario()).containsExactly("Bob", "false", "no-tooltip");
    }
}
