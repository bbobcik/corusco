package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DialogKeyboardExampleTest {

    @Test
    void dialogKeyboardExampleShowsEscapeAndDefaultButton() {
        assertThat(DialogKeyboardExample.runScenario()).containsExactly(
                "defaultButton=OK",
                "closedByEscape=true",
                "resetCalls=1",
                "defaultRestored=true"
        );
    }
}
