package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoadableValueExampleTest {

    @Test
    void loadableValueExampleShowsDetachAndRefreshLifecycle() {
        assertThat(LoadableValueExample.runScenario()).containsExactly(
                "attached=false",
                "customer-1",
                "customer-1",
                "attached=false",
                "customer-2",
                "customer-3",
                "loads=3"
        );
    }
}
