package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DirtyCancelDialogExampleTest {

    @Test
    void dirtyCancelDialogExampleShowsConfirmationDecisions() {
        assertThat(DirtyCancelDialogExample.runScenario()).containsExactly(
                "cleanClosed=true",
                "cleanReset=1",
                "dirtyRejected=false",
                "dirtyStillOpen=true",
                "dirtyReset=0",
                "dirtyConfirmed=true",
                "confirmedReset=1"
        );
    }
}
