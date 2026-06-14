package cz.auderis.corusco.examples.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LifecycleCleanupExampleTest {

    @Test
    void scopedCleanupRemovesExampleListeners() {
        assertThat(LifecycleCleanupExample.remainingListenersAfterCleanup())
                .isZero();
    }
}
