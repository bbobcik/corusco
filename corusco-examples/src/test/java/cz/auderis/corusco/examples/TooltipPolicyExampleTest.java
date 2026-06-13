package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.tooltip.TooltipPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TooltipPolicyExampleTest {

    @Test
    void tooltipPolicyExampleComposesGeneratedAndDynamicContent() {
        assertThat(TooltipPolicyExample.runScenario()).containsExactly(
                "Customer name is required",
                "Save is disabled until the required fields are valid",
                "Customer display name",
                TooltipPolicy.DEFAULT_HELP_INDICATOR
        );
    }
}
