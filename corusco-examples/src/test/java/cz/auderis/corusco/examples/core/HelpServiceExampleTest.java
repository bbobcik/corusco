package cz.auderis.corusco.examples.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelpServiceExampleTest {

    @Test
    void helpServiceExampleDispatchesGeneratedHelpTopic() {
        assertThat(HelpServiceExample.runScenario()).containsExactly(
                "generated-customer-table/name",
                "table-header",
                "1"
        );
    }
}
