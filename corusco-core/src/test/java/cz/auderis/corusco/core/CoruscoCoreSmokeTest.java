package cz.auderis.corusco.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoruscoCoreSmokeTest {

    @Test
    void coreModuleNameIsAvailable() {
        assertThat(CoruscoCore.moduleName())
                .isEqualTo("corusco-core");
    }
}
