package cz.auderis.corusco.examples.tables;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableHeaderTooltipExampleTest {

    @Test
    void tableHeaderTooltipExampleUsesGeneratedColumnResources() {
        assertThat(TableHeaderTooltipExample.runScenario()).containsExactly("Customer display name");
    }
}
