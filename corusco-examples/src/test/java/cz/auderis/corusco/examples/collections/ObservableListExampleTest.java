package cz.auderis.corusco.examples.collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObservableListExampleTest {

    @Test
    void observableListExampleShowsBatchAndMoveEvents() {
        assertThat(ObservableListExample.runScenario()).containsExactly("3", "1", "bravo,alpha");
    }
}
