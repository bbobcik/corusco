package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DetachableScopeExampleTest {

    @Test
    void detachableScopeExampleDetachesRegisteredModels() {
        assertThat(DetachableScopeExample.runScenario()).containsExactly(
                "customer-1",
                "order-1",
                "valueAttached=false",
                "rowsAttached=false",
                "customer-2",
                "order-2",
                "valueLoads=2",
                "rowLoads=2"
        );
    }
}
