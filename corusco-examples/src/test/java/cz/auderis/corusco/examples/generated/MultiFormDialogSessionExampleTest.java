package cz.auderis.corusco.examples.generated;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiFormDialogSessionExampleTest {

    @Test
    void multiFormDialogSessionComposesGeneratedChildrenAndDialogActions() {
        assertThat(MultiFormDialogSessionExample.runScenario()).containsExactly(
                "2",
                "true",
                "true",
                "false",
                "true",
                "true",
                "Acme Portal",
                "true",
                "Acme",
                "false",
                "AbstractCustomerProfileFormModel",
                "true",
                "1",
                "0",
                "1",
                "0"
        );
    }
}
