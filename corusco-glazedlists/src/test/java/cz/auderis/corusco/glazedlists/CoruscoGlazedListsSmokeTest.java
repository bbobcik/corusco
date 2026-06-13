package cz.auderis.corusco.glazedlists;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoruscoGlazedListsSmokeTest {

    @Test
    void exposesModuleName() {
        assertThat(CoruscoGlazedLists.moduleName()).isEqualTo("corusco-glazedlists");
    }
}
