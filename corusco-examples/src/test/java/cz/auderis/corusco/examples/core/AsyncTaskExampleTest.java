package cz.auderis.corusco.examples.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncTaskExampleTest {

    @Test
    void asyncTaskExampleRunsTaskAndCallbacks() {
        assertThat(AsyncTaskExample.runScenario()).containsExactly(
                "serviceBusy=true",
                "taskBusy=true",
                "serviceBusy=false",
                "success=loaded",
                "result=loaded",
                "taskBusy=false"
        );
    }
}
