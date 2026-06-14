package cz.auderis.corusco.examples.tables;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableStateExampleTest {

    @Test
    void tableStateExampleMergesStoredStateWithGeneratedDescriptor() {
        assertThat(TableStateExample.runScenario()).containsExactly(
                "generated-customer-table/orders:70:false",
                "generated-customer-table/customer-name:320:true",
                "1",
                "generated-customer-table/orders:0"
        );
    }
}
