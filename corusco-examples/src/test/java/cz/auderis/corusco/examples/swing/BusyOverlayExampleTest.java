package cz.auderis.corusco.examples.swing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusyOverlayExampleTest {

    @Test
    void busyOverlayExampleShowsBlockingAndCleanup() {
        assertThat(BusyOverlayExample.runScenario()).containsExactly(
                "initialBusy=false",
                "busyConsumesInput=true",
                "restored=true"
        );
    }
}
