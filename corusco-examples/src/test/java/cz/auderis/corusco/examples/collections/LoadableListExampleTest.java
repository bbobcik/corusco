package cz.auderis.corusco.examples.collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoadableListExampleTest {

    @Test
    void loadableListExampleShowsLazyRowsAndDetach() {
        assertThat(LoadableListExample.runScenario()).containsExactly(
                "attached=false",
                "row-1",
                "row-1",
                "row-1,local",
                "attached=false",
                "row-2",
                "loads=2"
        );
    }
}
