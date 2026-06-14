package cz.auderis.corusco.examples.collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransformedListExampleTest {

    @Test
    void transformedListExampleShowsSortedAndMappedViews() {
        assertThat(TransformedListExample.runScenario()).containsExactly(
                "Alpha,Beta",
                "Beta:2,Alpha:5",
                "Apex,Beta,Delta",
                "Beta:2,Delta:6,Apex:1",
                "Delta,Beta,Apex",
                "Delta,Beta,Apex",
                "Beta:2,Delta:6,Apex:1"
        );
    }
}
