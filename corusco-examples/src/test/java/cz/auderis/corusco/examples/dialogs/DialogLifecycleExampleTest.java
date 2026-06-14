package cz.auderis.corusco.examples.dialogs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DialogLifecycleExampleTest {

    @Test
    void dialogLifecycleExampleShowsCleanupOwnership() {
        assertThat(DialogLifecycleExample.runScenario()).containsExactly(
                "detached",
                "binding",
                "late",
                "dialogClosed=true"
        );
    }
}
