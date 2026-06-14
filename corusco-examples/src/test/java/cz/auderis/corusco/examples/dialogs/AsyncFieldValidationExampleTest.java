package cz.auderis.corusco.examples.dialogs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncFieldValidationExampleTest {

    @Test
    void asyncFieldValidationExampleIgnoresStaleResult() {
        assertThat(AsyncFieldValidationExample.runScenario()).containsExactly(
                "busyAfterEdit=true",
                "messages=[]",
                "busyAfterCurrent=false"
        );
    }
}
