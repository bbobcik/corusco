package cz.auderis.corusco.examples.collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EdtObservableListExampleTest {

    @Test
    void runsScenario() {
        assertThat(EdtObservableListExample.runScenario())
                .containsExactly("true", "inserted:1:[approved]", "draft,approved");
    }
}
