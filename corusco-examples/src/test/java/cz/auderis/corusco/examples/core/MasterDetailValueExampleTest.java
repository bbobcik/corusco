package cz.auderis.corusco.examples.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MasterDetailValueExampleTest {

    @Test
    void masterDetailValueExampleShowsActiveAndDetachedLoading() {
        assertThat(MasterDetailValueExample.runScenario()).containsExactly(
                "attached=false",
                "customer-1-detail-1",
                "customer-2-detail-2",
                "attached=false",
                "customer-3-detail-3",
                "loads=3"
        );
    }
}
