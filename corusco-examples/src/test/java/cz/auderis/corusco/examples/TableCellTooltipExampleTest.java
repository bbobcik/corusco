package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableCellTooltipExampleTest {

    @Test
    void tableCellTooltipExampleUsesGeneratedColumnResources() {
        assertThat(TableCellTooltipExample.runScenario()).containsExactly("Customer display name");
    }
}
