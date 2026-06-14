package cz.auderis.corusco.examples.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourcesExampleTest {

    @Test
    void resourcesExampleResolvesGeneratedResourceKeys() {
        assertThat(ResourcesExample.runScenario()).containsExactly(
                "Customer",
                "Customer display name",
                "orders fallback"
        );
    }
}
