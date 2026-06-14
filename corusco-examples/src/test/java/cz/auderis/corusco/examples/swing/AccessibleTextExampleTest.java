package cz.auderis.corusco.examples.swing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessibleTextExampleTest {

    @Test
    void accessibleTextExampleUsesGeneratedDescriptorResources() {
        assertThat(AccessibleTextExample.runScenario()).containsExactly(
                "Customer name",
                "Enter the customer display name"
        );
    }
}
